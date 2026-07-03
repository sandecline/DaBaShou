/**
 * 统一网络请求封装
 * 基于 wx.request 的 Promisify 封装
 * 功能：自动带 Token、401 自动刷新、统一错误处理、Mock 降级
 */

import type { ApiResponse } from '../types/api-response';
import { API_BASE_URL, USE_MOCK } from '../config/api';

// TODO: 部署后替换为真实域名
const BASE_URL = API_BASE_URL;
const TOKEN_KEY = 'dabashou_token';
const REFRESH_TOKEN_KEY = 'refresh_token';

// TODO: 后端未就绪时启用 Mock 模式
const MOCK_MODE = USE_MOCK;
const MOCK_DELAY = 120;

/** Token 刷新最大重试次数 */
const MAX_REFRESH_RETRIES = 3;

/** 移除 data 中的 undefined/null 值，避免 wx.request 序列化为字符串 "undefined" */
function cleanParams(data?: Record<string, unknown>): Record<string, unknown> {
  if (!data) return {};
  const cleaned: Record<string, unknown> = {};
  for (const [k, v] of Object.entries(data)) {
    if (v !== undefined && v !== null) cleaned[k] = v;
  }
  return cleaned;
}

interface RequestOptions {
  url: string;
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE';
  data?: Record<string, unknown>;
  header?: Record<string, string>;
  showLoading?: boolean;
  loadingText?: string;
}

// =====================================================================
//                                核心请求
// =====================================================================

function request<T = unknown>(options: RequestOptions): Promise<ApiResponse<T>> {
  if (MOCK_MODE) {
    return mockRequest<T>(options);
  }

  return new Promise((resolve, reject) => {
    const isAuthEndpoint = options.url.includes('/v1/auth/');
    const token = isAuthEndpoint ? '' : wx.getStorageSync(TOKEN_KEY);
    if (options.showLoading) {
      wx.showLoading({ title: options.loadingText || '加载中...', mask: true });
    }
    wx.request({
      url: `${BASE_URL}${options.url}`,
      method: options.method || 'GET',
      data: cleanParams(options.data),
      header: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...options.header,
      },
      success(res) {
        if (res.statusCode === 401) {
          if (isAuthEndpoint) {
            // 登录/注册等认证端点返回 401，直接拒绝，不尝试刷新 token
            const body = res.data as { msg?: string };
            reject({ code: 401, msg: body?.msg || '用户名或密码错误', data: res.data });
            return;
          }
          console.log('[Request] Token 过期，尝试刷新...');
          return refreshTokenAndRetry<T>(options).then(resolve).catch(reject);
        }
        if (res.statusCode >= 200 && res.statusCode < 300) {
          const body = res.data as ApiResponse<T>;
          if (body?.code === 401 && !isAuthEndpoint) {
            console.log('[Request] Token 过期，尝试刷新...');
            return refreshTokenAndRetry<T>(options).then(resolve).catch(reject);
          }
          if (body && typeof body.code === 'number' && body.code !== 200) {
            reject(body);
            return;
          }
          resolve(body);
        } else {
          const body = res.data as { msg?: string };
          reject({ code: res.statusCode, msg: body?.msg || '请求失败', data: res.data });
        }
      },
      fail(err) {
        console.error('[Request] 网络错误:', err);
        reject({ code: -1, msg: '网络连接失败，请稍后重试', detail: err });
      },
      complete() {
        if (options.showLoading) wx.hideLoading();
      },
    });
  });
}

let _refreshRetryCount = 0;
let _isReLaunching = false; // 防止并发 reLaunch 导致路由冲突
let _isRefreshing = false; // 防止并发刷新 Token
let _refreshQueue: Array<{ resolve: (value: ApiResponse<unknown>) => void; reject: (reason?: unknown) => void; options: RequestOptions }> = [];

function safeReLaunch() {
  if (_isReLaunching) return;
  _isReLaunching = true;
  const app = getApp();
  app.clearSession();
  wx.reLaunch({ url: '/pages/index/index', fail() { _isReLaunching = false; } });
}

async function refreshTokenAndRetry<T>(options: RequestOptions): Promise<ApiResponse<T>> {
  if (_isRefreshing) {
    // 已有刷新请求在进行，排队等待结果
    return new Promise<ApiResponse<T>>((resolve, reject) => {
      _refreshQueue.push({ resolve: resolve as (value: ApiResponse<unknown>) => void, reject, options });
    });
  }

  _isRefreshing = true;
  _refreshRetryCount++;
  if (_refreshRetryCount > MAX_REFRESH_RETRIES) {
    _refreshRetryCount = 0;
    _isRefreshing = false;
    _refreshQueue.forEach((q) => { q.reject({ code: 401, msg: '登录已过期，请重新登录' }); });
    _refreshQueue = [];
    safeReLaunch();
    throw { code: 401, msg: '登录已过期，请重新登录' };
  }
  try {
    const refreshToken = wx.getStorageSync(REFRESH_TOKEN_KEY);
    if (!refreshToken) {
      _isRefreshing = false;
      _refreshQueue.forEach((q) => { q.reject({ code: 401, msg: '登录已过期，请重新登录' }); });
      _refreshQueue = [];
      safeReLaunch();
      throw { code: 401, msg: '登录已过期，请重新登录' };
    }
    const res = await rawRequest<{ accessToken: string; refreshToken: string }>({
      url: '/v1/auth/refresh',
      method: 'POST',
      data: { refreshToken },
    });
    wx.setStorageSync(TOKEN_KEY, res.data.accessToken);
    wx.setStorageSync(REFRESH_TOKEN_KEY, res.data.refreshToken);
    _refreshRetryCount = 0;
    _isRefreshing = false;
    // 重试排队请求
    const queue = _refreshQueue;
    _refreshQueue = [];
    queue.forEach((q) => {
      request<unknown>(q.options).then(q.resolve).catch(q.reject);
    });
    return request<T>(options);
  } catch (err) {
    _refreshRetryCount = 0;
    _isRefreshing = false;
    _refreshQueue.forEach((q) => { q.reject(err); });
    _refreshQueue = [];
    throw err;
  }
}

function rawRequest<T = unknown>(options: RequestOptions): Promise<ApiResponse<T>> {
  return new Promise((resolve, reject) => {
    const isAuthEndpoint = options.url.includes('/v1/auth/');
    const token = isAuthEndpoint ? '' : wx.getStorageSync(TOKEN_KEY);
    wx.request({
      url: `${BASE_URL}${options.url}`,
      method: options.method || 'POST',
      data: cleanParams(options.data),
      header: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
      success(res) {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          const body = res.data as ApiResponse<T>;
          if (body && typeof body.code === 'number' && body.code !== 200) {
            reject(body);
            return;
          }
          resolve(body);
        }
        else reject({ code: res.statusCode, msg: '刷新失败' });
      },
      fail(err) { reject({ code: -1, msg: '网络错误', detail: err }); },
    });
  });
}

// =====================================================================
//                              Mock 数据
// =====================================================================

const MOCK_ME = {
  id: 1001, nickname: '张三', avatar: '',
  trustLevel: '靠谱' as const, trustScore: 3.5,
};

const MOCK_CATEGORIES = [
  { id: 1, name: '学业辅导', icon: 'book', sortOrder: 1, tags: [
    { id: 1, name: '数学', categoryId: 1 }, { id: 2, name: '英语', categoryId: 1 }, { id: 3, name: '物理', categoryId: 1 },
  ]},
  { id: 2, name: '编程开发', icon: 'code', sortOrder: 2, tags: [
    { id: 4, name: 'Python', categoryId: 2 }, { id: 5, name: 'C语言', categoryId: 2 }, { id: 6, name: 'Java', categoryId: 2 },
  ]},
  { id: 3, name: '生活服务', icon: 'home', sortOrder: 3, tags: [
    { id: 7, name: '搬运', categoryId: 3 }, { id: 8, name: '维修', categoryId: 3 }, { id: 9, name: '跑腿', categoryId: 3 },
  ]},
  { id: 4, name: '设计创作', icon: 'palette', sortOrder: 4, tags: [
    { id: 10, name: '海报设计', categoryId: 4 }, { id: 11, name: '视频剪辑', categoryId: 4 },
  ]},
];

const _tagCache = new Map<number, { tag: { id: number; name: string; categoryId: number }; categoryName: string }>();
function findTag(tagId: number) {
  if (_tagCache.has(tagId)) return _tagCache.get(tagId)!;
  for (const cat of MOCK_CATEGORIES) {
    const tag = cat.tags.find((t) => t.id === tagId);
    if (tag) {
      const result = { tag, categoryName: cat.name };
      _tagCache.set(tagId, result);
      return result;
    }
  }
  const fallback = { tag: { id: tagId, name: '未知', categoryId: 0 }, categoryName: '未知' };
  _tagCache.set(tagId, fallback);
  return fallback;
}

let _mockId = 10000;
function nextId() { return ++_mockId; }
/** 通用日期补零辅助函数 */
function pad(n: number) { return String(n).padStart(2, '0'); }
function nowStr() {
  const d = new Date();
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
}
/** n天前的时间字符串 */
function daysAgo(n: number) {
  const d = new Date(Date.now() - n * 86400000);
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
}
/** n天后的 ISO 格式截止时间 */
function isoDaysLater(n: number) {
  const d = new Date(Date.now() + n * 86400000);
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T23:59:59`;
}

// ── 内存 Store ──

const mockShelfStore: Record<string, unknown>[] = [
  {
    id: 1, title: 'Python 编程辅导', description: '帮助同学解决 Python 编程问题',
    pointPrice: 50, durationMinutes: 60, locationType: 1, images: [],
    status: 1, viewCount: 128, orderCount: 5, userId: 1002,
    skillTagId: 4, categoryName: '编程开发', tagName: 'Python',
    nickname: '李学霸', avatar: '', userName: '李学霸', userAvatar: '', trustScore: 4.5,
    timeSlots: [],
    createTime: daysAgo(2),
  },
  {
    id: 2, title: '高数考前辅导', description: '期末高数复习，帮你理清重难点',
    pointPrice: 30, durationMinutes: 90, locationType: 3, images: [],
    status: 1, viewCount: 85, orderCount: 3, userId: 1001,
    skillTagId: 1, categoryName: '学业辅导', tagName: '数学',
    nickname: '张三', avatar: '', userName: '张三', userAvatar: '', trustScore: 3.5,
    timeSlots: [],
    createTime: daysAgo(5),
  },
];

const mockDemandStore: Record<string, unknown>[] = [
  {
    id: 1, title: '急需帮忙搬宿舍', description: '下周六搬出七号楼，大约2小时',
    pointReward: 30, deadline: isoDaysLater(14), demandType: 1, demandTypeDesc: '求助悬赏',
    locationType: 2, isUrgent: false,
    status: 1, statusDesc: '待接单', skillTagId: 7, skillTagName: '搬运', tagName: '搬运',
    userId: 1003, nickname: '萌新小白', avatar: '', trustScore: 1.5,
    createTime: daysAgo(2), images: [],
  },
  {
    id: 2, title: '期末论文排版求助', description: 'Word 排版一直对不齐，求大佬帮看看',
    pointReward: 20, deadline: isoDaysLater(5), demandType: 1, demandTypeDesc: '求助悬赏',
    locationType: 1, isUrgent: true,
    status: 1, statusDesc: '待接单', skillTagId: 1, skillTagName: '学业辅导', tagName: '其他',
    userId: 1004, nickname: '学渣小王', avatar: '', trustScore: 2.0,
    createTime: daysAgo(1), images: [],
  },
  {
    id: 3, title: '帮忙修电风扇', description: '宿舍电风扇不转了，有人会修吗',
    pointReward: 15, deadline: isoDaysLater(10), demandType: 1, demandTypeDesc: '求助悬赏',
    locationType: 2, isUrgent: false,
    status: 1, statusDesc: '待接单', skillTagId: 8, skillTagName: '维修', tagName: '维修',
    userId: 1001, nickname: '张三', avatar: '', trustScore: 3.5,
    createTime: daysAgo(0), images: [],
  },
  {
    id: 4, title: 'Python 爬虫需要指导', description: 'BeautifulSoup 解析总是报错，求指导',
    pointReward: 40, deadline: isoDaysLater(7), demandType: 1, demandTypeDesc: '求助悬赏',
    locationType: 1, isUrgent: false,
    status: 2, statusDesc: '已接单', skillTagId: 4, skillTagName: 'Python', tagName: 'Python',
    userId: 1002, nickname: '李学霸', avatar: '', trustScore: 4.5,
    createTime: daysAgo(3), images: [],
  },
  {
    id: 5, title: '代取快递', description: '西门菜鸟驿站，有偿代取',
    pointReward: 5, deadline: isoDaysLater(3), demandType: 2, demandTypeDesc: '跑腿代取',
    locationType: 2, isUrgent: true,
    status: 1, statusDesc: '待接单', skillTagId: 9, skillTagName: '跑腿', tagName: '跑腿',
    userId: 1003, nickname: '萌新小白', avatar: '', trustScore: 1.5,
    createTime: daysAgo(0), images: [],
  },
];

const mockOrderStore: Record<string, unknown>[] = [
  {
    id: 1, orderNo: 'DB202607010001', shelfTitle: 'Python 编程辅导',
    pointAmount: 50, status: 1, statusName: '待支付',
    buyerId: 1003, buyerNickname: '李四', buyerAvatar: '',
    sellerId: 1001, sellerNickname: '张三', sellerAvatar: '',
    counterpartNickname: '张三', counterpartAvatar: '',
    skillTagName: 'Python', durationMinutes: 60, skillShelfId: 1, remark: '',
    buyerCode: 'B1234', sellerCode: 'S5678',
    buyerVerified: false, sellerVerified: false,
    buyerConfirmed: false, sellerConfirmed: false,
    createTime: daysAgo(2),
  },
];

const mockMessageStore: Record<string, unknown>[] = [];

// =====================================================================
//                             Mock 路由
// =====================================================================

function getMockData<T>(options: RequestOptions): T {
  const { url, method, data } = options;
  const isPost = method === 'POST';
  const isGet = !method || method === 'GET';

  // ── 技能分类 ──
  if (url.includes('/skills/categories')) {
    return MOCK_CATEGORIES as unknown as T;
  }

  // ── 更新货架 PUT /v1/shelves/:id ──
  if (method === 'PUT' && url.match(/\/shelves\/\d+$/)) {
    const id = Number(url.match(/\/shelves\/(\d+)$/)?.[1]);
    const params = (data || {}) as Record<string, unknown>;
    const shelf = mockShelfStore.find((s) => s.id === id);
    if (!shelf) throw { code: 404, msg: '货架不存在' };
    if (params.skillTagId !== undefined) {
      const { tag, categoryName } = findTag(Number(params.skillTagId));
      (params as Record<string, unknown>).tagName = tag.name;
      (params as Record<string, unknown>).categoryName = categoryName;
    }
    Object.assign(shelf, params);
    console.log('[Mock] 货架已更新:', id);
    return shelf as unknown as T;
  }

  // ── 发布货架 POST /v1/shelves ──
  if (isPost && url.includes('/shelves') && !url.includes('/shelves/')) {
    const params = (data || {}) as Record<string, unknown>;
    const skillTagId = Number(params.skillTagId) || 0;
    const { tag, categoryName } = findTag(skillTagId);
    const newSkill = {
      id: nextId(),
      userId: MOCK_ME.id,
      skillTagId,
      title: String(params.title || ''),
      description: String(params.description || ''),
      pointPrice: Number(params.pointPrice) || 0,
      durationMinutes: Number(params.durationMinutes) || 60,
      locationType: Number(params.locationType) || 1,
      images: (params.images as string[]) || [],
      timeSlots: (params.timeSlots as Record<string, unknown>[]) || [],
      status: 1,
      viewCount: 0,
      orderCount: 0,
      categoryName,
      tagName: tag.name,
      nickname: MOCK_ME.nickname, avatar: '', trustScore: MOCK_ME.trustScore,
      createTime: nowStr(),
    };
    mockShelfStore.unshift(newSkill);
    console.log('[Mock] 新货架已发布:', newSkill.title, 'id=', newSkill.id);
    return { id: newSkill.id } as unknown as T;
  }

  // ── 我的货架 GET /v1/shelves/mine ──
  if (isGet && url.includes('/shelves/mine')) {
    const params = (data || {}) as Record<string, unknown>;
    const list = mockShelfStore.filter((s) => s.userId === MOCK_ME.id);
    const pageNum = Number(params.pageNum) || 1;
    const pageSize = Number(params.pageSize) || 10;
    const start = (pageNum - 1) * pageSize;
    return { list: list.slice(start, start + pageSize), total: list.length, pageNum, pageSize } as unknown as T;
  }

  // ── 货架列表 GET /v1/shelves ──
  if (isGet && url.includes('/shelves') && !url.includes('/shelves/')) {
    const params = (data || {}) as Record<string, unknown>;
    let list = [...mockShelfStore];
    const categoryId = Number(params.categoryId);
    if (categoryId) {
      list = list.filter((s) => {
        const info = findTag(s.skillTagId as number);
        return info.tag.categoryId === categoryId;
      });
    }
    const keyword = params.keyword as string;
    if (keyword) {
      list = list.filter((s) => String(s.title).includes(keyword) || String(s.description).includes(keyword));
    }
    const pageNum = Number(params.pageNum) || 1;
    const pageSize = Number(params.pageSize) || 10;
    const start = (pageNum - 1) * pageSize;
    return { list: list.slice(start, start + pageSize), total: list.length, pageNum, pageSize } as unknown as T;
  }

  // ── 货架详情 GET /v1/shelves/:id ──
  if (isGet && url.match(/\/shelves\/\d+$/)) {
    const id = Number(url.match(/\/shelves\/(\d+)$/)?.[1]);
    return (mockShelfStore.find((s) => s.id === id) || mockShelfStore[0]) as unknown as T;
  }

  // ── 货架空闲时段 GET /v1/shelves/:id/timeslots ──
  if (isGet && url.match(/\/shelves\/\d+\/timeslots$/)) {
    const id = Number(url.match(/\/shelves\/(\d+)\/timeslots$/)?.[1]);
    const shelf = mockShelfStore.find((s) => s.id === id);
    return (shelf?.timeSlots || []) as unknown as T;
  }

  // ── 发布需求 POST /v1/demands ──
  if (isPost && url.match(/\/demands\/?$/) && !url.includes('/demands/')) {
    const params = (data || {}) as Record<string, unknown>;
    const skillTagId = Number(params.skillTagId) || 0;
    const { tag } = findTag(skillTagId);
    const newDemand = {
      id: nextId(), userId: MOCK_ME.id, skillTagId, skillTagName: tag.name,
      title: String(params.title || ''), description: String(params.description || ''),
      pointReward: Number(params.pointReward) || 0, deadline: String(params.deadline || ''),
      demandType: Number(params.demandType) || 1,
      locationType: Number(params.locationType) || 1,
      images: (params.images as string[]) || [],
      status: 1,
      nickname: MOCK_ME.nickname, avatar: '', trustScore: MOCK_ME.trustScore,
      createTime: nowStr(),
    };
    mockDemandStore.unshift(newDemand);
    console.log('[Mock] 新需求已发布:', newDemand.title, 'id=', newDemand.id);
    return { id: newDemand.id } as unknown as T;
  }

  // ── 需求列表 GET /v1/demands ──
  if (isGet && url.match(/\/demands\/?$/) && !url.includes('/demands/')) {
    const params = (data || {}) as Record<string, unknown>;
    let list = [...mockDemandStore];
    const keyword = params.keyword as string;
    if (keyword) list = list.filter((d) => String(d.title).includes(keyword) || String(d.description).includes(keyword));
    const pageNum = Number(params.pageNum) || 1;
    const pageSize = Number(params.pageSize) || 10;
    const start = (pageNum - 1) * pageSize;
    return { list: list.slice(start, start + pageSize), total: list.length, pageNum, pageSize } as unknown as T;
  }

  // ── 需求详情 GET /v1/demands/:id ──
  if (isGet && url.match(/\/demands\/\d+$/)) {
    const id = Number(url.match(/\/demands\/(\d+)$/)?.[1]);
    return (mockDemandStore.find((d) => d.id === id) || mockDemandStore[0]) as unknown as T;
  }

  // ── 我的需求 GET /v1/demands/mine ──
  if (isGet && url.includes('/demands/mine')) {
    const params = (data || {}) as Record<string, unknown>;
    const list = mockDemandStore.filter((d) => d.userId === MOCK_ME.id);
    const pageNum = Number(params.pageNum) || 1;
    const pageSize = Number(params.pageSize) || 10;
    const start = (pageNum - 1) * pageSize;
    return { list: list.slice(start, start + pageSize), total: list.length, pageNum, pageSize } as unknown as T;
  }

  // ── 接单（揭榜）POST /v1/demands/:id/accept ──
  if (isPost && url.match(/\/demands\/\d+\/accept$/)) {
    const id = Number(url.match(/\/demands\/(\d+)\/accept$/)?.[1]);
    const demand = mockDemandStore.find((d) => d.id === id);
    if (!demand || demand.status !== 1) throw { code: 400, msg: '该需求不可接单' };
    demand.status = 2; demand.statusDesc = '已接单';
    // 同时创建关联订单
    const orderId = nextId();
    const orderNo = 'DB' + Date.now();
    const buyerCode = 'B' + String(Math.floor(Math.random() * 10000)).padStart(4, '0');
    const sellerCode = 'S' + String(Math.floor(Math.random() * 10000)).padStart(4, '0');
    mockOrderStore.unshift({
      id: orderId, orderNo, shelfTitle: demand.title,
      pointAmount: demand.pointReward, status: 3, statusName: '服务中',
      buyerId: demand.userId, buyerNickname: demand.nickname, buyerAvatar: '',
      sellerId: MOCK_ME.id, sellerNickname: MOCK_ME.nickname, sellerAvatar: '',
      counterpartNickname: demand.nickname, counterpartAvatar: '',
      skillTagName: demand.skillTagName || demand.tagName, demandId: id,
      buyerCode, sellerCode,
      buyerVerified: true, sellerVerified: true, // 需求揭榜直接进入服务中
      buyerConfirmed: false, sellerConfirmed: false,
      remark: '来自求助看板',
      createTime: nowStr(),
    });
    console.log('[Mock] 已接单 + 订单已创建:', orderNo, 'id=', orderId);
    return {} as unknown as T;
  }

  // ── 取消需求 POST /v1/demands/:id/cancel ──
  if (isPost && url.match(/\/demands\/\d+\/cancel$/)) {
    const id = Number(url.match(/\/demands\/(\d+)\/cancel$/)?.[1]);
    const demand = mockDemandStore.find((d) => d.id === id);
    if (demand) { demand.status = 0; demand.statusDesc = '已取消'; }
    console.log('[Mock] 需求已取消:', id);
    return {} as unknown as T;
  }

  // ── 从货架创建订单 POST /v1/orders/from-shelf ──
  if (isPost && url.includes('/orders/from-shelf')) {
    const params = (data || {}) as Record<string, unknown>;
    const shelfId = Number(params.skillShelfId || params.shelfId);
    const shelf = mockShelfStore.find((s) => s.id === shelfId);
    if (!shelf) throw { code: 400, msg: '技能货架不存在' };
    if (shelf.userId === MOCK_ME.id) throw { code: 400, msg: '不能购买自己的服务' };

    const orderId = nextId();
    const orderNo = 'DB' + Date.now();
    const buyerCode = 'B' + String(Math.floor(Math.random() * 10000)).padStart(4, '0');
    const sellerCode = 'S' + String(Math.floor(Math.random() * 10000)).padStart(4, '0');
    mockOrderStore.unshift({
      id: orderId, orderNo, shelfTitle: shelf.title,
      pointAmount: shelf.pointPrice, status: 1, statusName: '待支付',
      buyerId: MOCK_ME.id, buyerNickname: MOCK_ME.nickname, buyerAvatar: '',
      sellerId: shelf.userId, sellerNickname: shelf.nickname, sellerAvatar: '',
      counterpartNickname: shelf.nickname, counterpartAvatar: '',
      skillTagName: shelf.tagName, durationMinutes: shelf.durationMinutes, skillShelfId: shelfId,
      buyerCode, sellerCode,
      buyerVerified: false, sellerVerified: false,
      buyerConfirmed: false, sellerConfirmed: false,
      timeSlotId: params.timeSlotId || undefined, remark: params.remark || '',
      createTime: nowStr(),
    });
    shelf.orderCount = (Number(shelf.orderCount) || 0) + 1;
    console.log('[Mock] 新订单已创建:', orderNo, 'buyerCode=', buyerCode, 'sellerCode=', sellerCode);
    return { orderId, orderNo, buyerCode, sellerCode } as unknown as T;
  }

  // ── 订单列表 GET /v1/orders ──
  if (isGet && url.match(/\/orders\/?$/) && !url.includes('/orders/')) {
    const params = (data || {}) as Record<string, unknown>;
    const role = params.role as 'buyer' | 'seller' | undefined;
    const statusParam = params.status as string | number | undefined;
    let statusFilter: number[] | undefined;
    if (statusParam !== undefined && statusParam !== '') {
      const statusArr = String(statusParam).split(',').map((s) => Number(s.trim())).filter((n) => !Number.isNaN(n));
      if (statusArr.length > 0) statusFilter = statusArr;
    }
    let list = [...mockOrderStore];
    if (role === 'buyer') list = list.filter((o) => o.buyerId === MOCK_ME.id);
    else if (role === 'seller') list = list.filter((o) => o.sellerId === MOCK_ME.id);
    if (statusFilter) list = list.filter((o) => statusFilter!.includes(o.status as number));
    const pageNum = Number(params.pageNum) || 1;
    const pageSize = Number(params.pageSize) || 10;
    const start = (pageNum - 1) * pageSize;
    return { list: list.slice(start, start + pageSize), total: list.length, pageNum, pageSize } as unknown as T;
  }

  // ── 订单详情 / 状态 兜底 ──
  if (url.includes('/orders')) {
    if (isGet && url.match(/\/orders\/\d+$/)) {
      const id = Number(url.match(/\/orders\/(\d+)$/)?.[1]);
      return (mockOrderStore.find((o) => o.id === id) || mockOrderStore[0]) as unknown as T;
    }
    if (isGet && url.match(/\/orders\/\d+\/status$/)) {
      const id = Number(url.match(/\/orders\/(\d+)\/status$/)?.[1]);
      const o = mockOrderStore.find((x) => x.id === id);
      return { status: o?.status || 0, statusName: o?.statusName || '未知' } as unknown as T;
    }
    // ── 核销（双阶段）POST /v1/orders/:id/verify ──
    if (isPost && url.match(/\/orders\/\d+\/verify$/)) {
      const id = Number(url.match(/\/orders\/(\d+)\/verify$/)?.[1]);
      const params = (data || {}) as Record<string, unknown>;
      const order = mockOrderStore.find((o) => o.id === id);
      if (!order) throw { code: 404, msg: '订单不存在' };
      const phase = (params.phase || 'start') as 'start' | 'complete';
      const code = String(params.verifyCode || '');
      const myRole = order.buyerId === MOCK_ME.id ? 'buyer' : 'seller';

      if (phase === 'start') {
        // 启动核销：买家输入卖家码，卖家输入买家码
        if (order.status !== 1) throw { code: 400, msg: '当前状态不可启动核销' };
        const expected = myRole === 'buyer' ? order.sellerCode : order.buyerCode;
        if (code !== expected) throw { code: 400, msg: '核销码错误' };
        order[myRole === 'buyer' ? 'buyerVerified' : 'sellerVerified'] = true;
        if (order.buyerVerified && order.sellerVerified) {
          order.status = 3; order.statusName = '服务中';
          order.serviceStartTime = nowStr();
          console.log('[Mock] 订单核销完成，进入服务中:', id);
        }
      } else {
        // 确认完成：买家输入卖家码，卖家输入买家码
        if (order.status !== 3) throw { code: 400, msg: '当前状态不可确认完成' };
        const expected = myRole === 'buyer' ? order.sellerCode : order.buyerCode;
        if (code !== expected) throw { code: 400, msg: '确认码错误' };
        order[myRole === 'buyer' ? 'buyerConfirmed' : 'sellerConfirmed'] = true;
        if (order.buyerConfirmed && order.sellerConfirmed) {
          order.status = 5; order.statusName = '已完成';
          order.completeTime = nowStr();
          console.log('[Mock] 订单已完成（双方确认通过）:', id);
        }
      }
      return { status: order.status, statusName: order.statusName } as unknown as T;
    }

    // ── 退款 POST /v1/orders/:id/refund ──
    if (isPost && url.match(/\/orders\/\d+\/refund$/)) {
      const id = Number(url.match(/\/orders\/(\d+)\/refund$/)?.[1]);
      const params = (data || {}) as Record<string, unknown>;
      const order = mockOrderStore.find((o) => o.id === id);
      if (!order) throw { code: 404, msg: '订单不存在' };
      if (![3, 4].includes(order.status as number)) throw { code: 400, msg: '当前状态不可退款' };
      const action = params.action as string;
      const myRole = (order.buyerId === MOCK_ME.id ? 'buyer' : 'seller') as 'buyer' | 'seller';

      if (action === 'approve') {
        // 同意退款：仅对方可操作，不允许自己同意自己的申请
        if (!order.refundRequesting) throw { code: 400, msg: '无待处理的退款请求' };
        if (order.refundRequester === myRole) throw { code: 400, msg: '不能同意自己发起的退款' };
        order.status = 6; order.statusName = '已退款';
        order.refundRequesting = false;
        console.log('[Mock] 退款已同意:', id, 'by', myRole);
        return { id, status: 6, statusName: '已退款' } as unknown as T;
      }
      // 申请退款
      if (order.refundRequesting) throw { code: 400, msg: '已有退款申请待处理' };
      order.refundRequesting = true;
      order.refundRequester = myRole;
      console.log('[Mock] 退款申请已提交:', id, 'by', myRole);
      return { id, refundRequesting: true } as unknown as T;
    }

    // ── 取消 ──
    if (isPost && url.match(/\/orders\/\d+\/cancel$/)) {
      const id = Number(url.match(/\/orders\/(\d+)\/cancel$/)?.[1]);
      const order = mockOrderStore.find((o) => o.id === id);
      if (!order) throw { code: 404, msg: '订单不存在' };
      const myRole = order.buyerId === MOCK_ME.id ? 'buyer' : 'seller';
      if (myRole !== 'seller') throw { code: 403, msg: '只有卖家可以取消订单' };
      if (order.status !== 1) throw { code: 400, msg: '当前状态不可取消' };
      order.status = 0; order.statusName = '已取消';
      return { id, status: 0, statusName: '已取消' } as unknown as T;
    }

    // ── 订单关联评价 ──
    if (isGet && url.match(/\/orders\/\d+\/review$/)) {
      // Mock 默认未评价，返回空对象
      return {} as unknown as T;
    }

    return { id: 1, orderNo: 'DB202607010001', status: 3, statusName: '服务中' } as unknown as T;
  }


  // ── 积分 ──
  if (url.includes('/points')) {
    if (isPost && url.includes('/points/freeze')) {
      // 创建订单时冻结积分：Mock 直接成功
      return {} as unknown as T;
    }
    if (url.includes('/points/settle')) {
      // 订单完成时结算积分
      return {} as unknown as T;
    }
    if (url.includes('/points/refund')) {
      // 取消/退款时退还积分
      return {} as unknown as T;
    }
    return {
      available: 500, frozen: 50, total: 550, balance: 500,
      totalEarned: 1200, totalSpent: 700,
    } as unknown as T;
  }


  // ── 会话列表 ──
  if (url.includes('/chat/sessions') && !url.includes('/read') && !url.match(/\/sessions\/\d+/)) {
    if (isPost) {
      const id = nextId();
      return { id } as unknown as T;
    }
    return [{ id: 1, otherUserId: 1001, otherNickname: '张三', otherAvatar: '', lastMessage: '好的，没问题！', lastTime: '2026-07-01 10:30:00', unreadCount: 2, orderId: 1, orderTitle: 'Python 编程辅导' }] as unknown as T;
  }

  // ── 发送消息 POST /v1/chat/send ──
  if (isPost && url.includes('/chat/send')) {
    const params = (data || {}) as Record<string, unknown>;
    const receiverId = Number(params.receiverId) || 0;
    const msgId = nextId();
    mockMessageStore.push({
      id: msgId, sessionId: receiverId, senderId: MOCK_ME.id,
      senderNickname: MOCK_ME.nickname, senderAvatar: '',
      receiverId, msgType: Number(params.msgType) || 1, content: String(params.content || ''),
      isRead: 0, createTime: nowStr(),
    });
    console.log('[Mock] 消息已存储:', String(params.content).substring(0, 20), 'to=', receiverId);
    return { id: msgId } as unknown as T;
  }

  // ── 消息列表 GET /v1/chat/messages ──
  if (isGet && url.includes('/chat/messages')) {
    const params = (data || {}) as Record<string, unknown>;
    const targetUserId = Number(params.targetUserId) || Number(params.sessionId) || 0;
    const myId = MOCK_ME.id;
    const list = mockMessageStore.filter((m) =>
      (m.senderId === myId && m.receiverId === targetUserId) ||
      (m.senderId === targetUserId && m.receiverId === myId)
    );
    const pageNum = Number(params.pageNum) || 1;
    const pageSize = Number(params.pageSize) || 20;
    const start = (pageNum - 1) * pageSize;
    return { list: list.slice(start, start + pageSize), total: list.length, pageNum, pageSize } as unknown as T;
  }

  // ── 用户资料 ──
  if (url.includes('/user/profile')) {
    return {
      id: MOCK_ME.id, nickname: MOCK_ME.nickname, avatar: '',
      campus: '清水河校区', building: '七号楼', bio: '计算机学院学生，擅长 Python',
      trustLevel: MOCK_ME.trustLevel, trustScore: MOCK_ME.trustScore, pointBalance: 500,
      stats: { helpCount: 12, helpedCount: 5, skillCount: 3, praiseRate: 95, registerDays: 30 },
    } as unknown as T;
  }

  // ── 评价 /v1/reviews/* ──
  if (url.includes('/reviews')) {
    const params = (data || {}) as Record<string, unknown>;
    const pageNum = Number(params.pageNum) || 1;
    const pageSize = Number(params.pageSize) || 10;
    if (isPost && url.match(/\/reviews\/?$/)) {
      // 提交评价
      return { id: nextId() } as unknown as T;
    }
    if (url.includes('/reviews/pending')) {
      return [{ orderId: 1, orderTitle: 'Python 编程辅导', targetUser: { id: 1001, nickname: '张三' } }] as unknown as T;
    }
    return {
      records: [{ id: 1, orderId: 1, orderTitle: 'Python 编程辅导', rating: 5, content: '非常耐心，讲解清晰！', images: [], isAnonymous: 0, reviewerId: 1003, reviewerNickname: '李四', reviewerAvatar: '', createTime: '2026-06-28 15:00:00' }],
      total: 1, pageNum, pageSize,
    } as unknown as T;
  }


  // ── 信用 ──
  if (url.includes('/credit')) {
    return { trustScore: 3.5, trustLevel: '靠谱', totalReviews: 8, positiveReviews: 7, neutralReviews: 1, negativeReviews: 0, logs: [] } as unknown as T;
  }

  // ── 登录 ──
  if (url.includes('/auth')) {
    return {
      accessToken: 'mock_jwt_header.payload_stub.signature',
      refreshToken: 'mock_refresh_jwt_header.payload_stub.signature',
      expiresIn: 86400,
      userId: MOCK_ME.id,
      nickname: MOCK_ME.nickname,
      avatar: '',
    } as unknown as T;
  }

  // 兜底
  return { list: [], total: 0, pageNum: 1, pageSize: 10 } as unknown as T;
}

// =====================================================================
//                           Mock 请求包装
// =====================================================================

function mockRequest<T>(options: RequestOptions): Promise<ApiResponse<T>> {
  console.log(`[Mock] ${options.method || 'GET'} ${options.url}`, options.data ? JSON.stringify(options.data).substring(0, 200) : '');
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      try {
        resolve({ code: 200, msg: 'success', data: getMockData<T>(options) });
      } catch (err) {
        console.error('[Mock] 处理失败:', err);
        reject(err);
      }
    }, MOCK_DELAY);
  });
}

// =====================================================================
//                            便捷方法
// =====================================================================

export const api = {
  get<T = unknown>(url: string, data?: Record<string, unknown>) { return request<T>({ url, method: 'GET', data }); },
  post<T = unknown>(url: string, data?: Record<string, unknown>) { return request<T>({ url, method: 'POST', data }); },
  put<T = unknown>(url: string, data?: Record<string, unknown>) { return request<T>({ url, method: 'PUT', data }); },
  delete<T = unknown>(url: string, data?: Record<string, unknown>) { return request<T>({ url, method: 'DELETE', data }); },
};

export { request, rawRequest, BASE_URL };
