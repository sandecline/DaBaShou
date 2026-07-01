/**
 * 统一网络请求封装
 * 基于 wx.request 的 Promisify 封装
 * 功能：自动带 Token、401 自动刷新、统一错误处理、Mock 降级
 */

import type { ApiResponse } from '../types/api-response';

// TODO: 部署后替换为真实域名
const BASE_URL = 'https://api.dabashou.example.com';

// TODO: 后端未就绪时启用 Mock 模式
const MOCK_MODE = true;
const MOCK_DELAY = 120;

/** Token 刷新最大重试次数 */
const MAX_REFRESH_RETRIES = 3;

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
    const token = wx.getStorageSync('access_token');
    if (options.showLoading) {
      wx.showLoading({ title: options.loadingText || '加载中...', mask: true });
    }
    wx.request({
      url: `${BASE_URL}${options.url}`,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...options.header,
      },
      success(res) {
        if (res.statusCode === 401) {
          console.log('[Request] Token 过期，尝试刷新...');
          return refreshTokenAndRetry<T>(options).then(resolve).catch(reject);
        }
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve(res.data as ApiResponse<T>);
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

async function refreshTokenAndRetry<T>(options: RequestOptions): Promise<ApiResponse<T>> {
  _refreshRetryCount++;
  if (_refreshRetryCount > MAX_REFRESH_RETRIES) {
    _refreshRetryCount = 0;
    const app = getApp();
    app.clearSession();
    wx.reLaunch({ url: '/pages/index/index' });
    throw { code: 401, msg: '登录已过期，请重新登录' };
  }
  try {
    const refreshToken = wx.getStorageSync('refresh_token');
    if (!refreshToken) {
      const app = getApp();
      app.clearSession();
      wx.reLaunch({ url: '/pages/index/index' });
      throw { code: 401, msg: '登录已过期，请重新登录' };
    }
    const res = await rawRequest<{ access_token: string; refresh_token: string }>({
      url: '/v1/auth/refresh',
      method: 'POST',
      data: { refresh_token: refreshToken },
    });
    wx.setStorageSync('access_token', res.data.access_token);
    wx.setStorageSync('refresh_token', res.data.refresh_token);
    _refreshRetryCount = 0;
    return request<T>(options);
  } catch (err) {
    _refreshRetryCount = 0;
    throw err;
  }
}

function rawRequest<T = unknown>(options: RequestOptions): Promise<ApiResponse<T>> {
  return new Promise((resolve, reject) => {
    const token = wx.getStorageSync('access_token');
    wx.request({
      url: `${BASE_URL}${options.url}`,
      method: options.method || 'POST',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
      success(res) {
        if (res.statusCode === 200) resolve(res.data as ApiResponse<T>);
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
function nowStr() {
  const d = new Date();
  const p = (n: number) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`;
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
    createTime: '2026-07-01 10:00:00',
  },
  {
    id: 2, title: '高数考前辅导', description: '期末高数复习，帮你理清重难点',
    pointPrice: 30, durationMinutes: 90, locationType: 3, images: [],
    status: 1, viewCount: 85, orderCount: 3, userId: 1001,
    skillTagId: 1, categoryName: '学业辅导', tagName: '数学',
    nickname: '张三', avatar: '', userName: '张三', userAvatar: '', trustScore: 3.5,
    timeSlots: [],
    createTime: '2026-06-28 14:20:00',
  },
];

const mockDemandStore: Record<string, unknown>[] = [
  {
    id: 1, title: '急需帮忙搬宿舍', description: '下周六搬出七号楼，大约2小时',
    pointReward: 30, deadline: '2026-07-08T23:59:59', demandType: 1, demandTypeDesc: '求助悬赏',
    locationType: 2, isUrgent: false,
    status: 1, statusDesc: '待接单', skillTagId: 7, skillTagName: '搬运', tagName: '搬运',
    userId: 1003, nickname: '萌新小白', avatar: '', trustScore: 1.5,
    createTime: '2026-07-01 08:00:00', images: [],
  },
];

const mockOrderStore: Record<string, unknown>[] = [
  {
    id: 1, orderNo: 'DB202607010001', title: 'Python 编程辅导',
    pointAmount: 50, status: 3, statusDesc: '服务中',
    buyerId: 1003, buyerNickname: '李四', buyerAvatar: '',
    sellerId: 1001, sellerNickname: '张三', sellerAvatar: '',
    counterpartNickname: '张三', counterpartAvatar: '',
    skillTagName: 'Python', durationMinutes: 60, skillShelfId: 1, remark: '',
    createTime: '2026-07-01 10:00:00',
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

  // ── 从货架创建订单 POST /v1/order/from-shelf ──
  if (isPost && url.includes('/order/from-shelf')) {
    const params = (data || {}) as Record<string, unknown>;
    const shelfId = Number(params.shelfId);
    const shelf = mockShelfStore.find((s) => s.id === shelfId);
    if (!shelf) throw { code: 400, msg: '技能货架不存在' };
    if (shelf.userId === MOCK_ME.id) throw { code: 400, msg: '不能购买自己的服务' };

    const orderId = nextId();
    const orderNo = 'DB' + Date.now();
    const buyerCode = 'B' + String(Math.floor(Math.random() * 10000)).padStart(4, '0');
    const sellerCode = 'S' + String(Math.floor(Math.random() * 10000)).padStart(4, '0');
    mockOrderStore.unshift({
      id: orderId, orderNo, title: shelf.title,
      pointAmount: shelf.pointPrice, status: 1, statusDesc: '待支付',
      buyerId: MOCK_ME.id, buyerNickname: MOCK_ME.nickname, buyerAvatar: '',
      sellerId: shelf.userId, sellerNickname: shelf.nickname, sellerAvatar: '',
      counterpartNickname: shelf.nickname, counterpartAvatar: '',
      skillTagName: shelf.tagName, durationMinutes: shelf.durationMinutes, skillShelfId: shelfId,
      buyerCode, sellerCode,
      timeSlotId: params.timeSlotId || undefined, remark: params.remark || '',
      createTime: nowStr(),
    });
    shelf.orderCount = (Number(shelf.orderCount) || 0) + 1;
    console.log('[Mock] 新订单已创建:', orderNo, 'buyerCode=', buyerCode, 'sellerCode=', sellerCode);
    return { orderId, orderNo, buyerCode, sellerCode } as unknown as T;
  }

  // ── 订单列表 GET /v1/order ──
  if (isGet && url.match(/\/order\/?$/) && !url.includes('/order/')) {
    const params = (data || {}) as Record<string, unknown>;
    const role = params.role as 'buyer' | 'seller' | undefined;
    const status = Number(params.status);
    let list = [...mockOrderStore];
    if (role === 'buyer') list = list.filter((o) => o.buyerId === MOCK_ME.id);
    else if (role === 'seller') list = list.filter((o) => o.sellerId === MOCK_ME.id);
    if (status) list = list.filter((o) => o.status === status);
    const pageNum = Number(params.pageNum) || 1;
    const pageSize = Number(params.pageSize) || 10;
    const start = (pageNum - 1) * pageSize;
    return { list: list.slice(start, start + pageSize), total: list.length, pageNum, pageSize } as unknown as T;
  }

  // ── 订单详情 / 状态 兜底 ──
  if (url.includes('/order')) {
    if (isGet && url.match(/\/order\/\d+$/)) {
      const id = Number(url.match(/\/order\/(\d+)$/)?.[1]);
      return (mockOrderStore.find((o) => o.id === id) || mockOrderStore[0]) as unknown as T;
    }
    if (isGet && url.match(/\/order\/\d+\/status$/)) {
      const id = Number(url.match(/\/order\/(\d+)\/status$/)?.[1]);
      const o = mockOrderStore.find((x) => x.id === id);
      return { status: o?.status || 0, statusDesc: o?.statusDesc || '未知' } as unknown as T;
    }
    // ── 接单 POST /v1/order/:id/accept ──
    if (isPost && url.match(/\/order\/\d+\/accept$/)) {
      const id = Number(url.match(/\/order\/(\d+)\/accept$/)?.[1]);
      const order = mockOrderStore.find((o) => o.id === id);
      if (!order || order.status !== 1) throw { code: 400, msg: '订单不可接单' };
      order.status = 3; order.statusDesc = '服务中';
      order.serviceStartTime = nowStr();
      console.log('[Mock] 订单已接单:', id);
      return { id, status: 3, statusDesc: '服务中' } as unknown as T;
    }

    // ── 完成服务（双向验证）POST /v1/order/:id/complete ──
    if (isPost && url.match(/\/order\/\d+\/complete$/)) {
      const id = Number(url.match(/\/order\/(\d+)\/complete$/)?.[1]);
      const params = (data || {}) as Record<string, unknown>;
      const order = mockOrderStore.find((o) => o.id === id);
      if (!order || order.status !== 3) throw { code: 400, msg: '当前状态不可完成' };
      const role = params.role as 'buyer' | 'seller';
      const code = params.code as string;
      // 验证：买家输入卖家码，卖家输入买家码
      const expected = role === 'buyer' ? order.sellerCode : order.buyerCode;
      if (code !== expected) throw { code: 400, msg: '验证码错误' };
      order[role === 'buyer' ? 'buyerVerified' : 'sellerVerified'] = true;
      if (order.buyerVerified && order.sellerVerified) {
        order.status = 5; order.statusDesc = '已完成';
        order.completeTime = nowStr();
        console.log('[Mock] 订单已完成（双向验证通过）:', id);
      }
      return { id, status: order.status, statusDesc: order.statusDesc } as unknown as T;
    }

    // ── 退款申请 POST /v1/order/:id/refund-request ──
    if (isPost && url.match(/\/order\/\d+\/refund-request$/)) {
      const id = Number(url.match(/\/order\/(\d+)\/refund-request$/)?.[1]);
      const params = (data || {}) as Record<string, unknown>;
      const order = mockOrderStore.find((o) => o.id === id);
      if (!order || ![3, 4].includes(order.status as number)) throw { code: 400, msg: '当前状态不可退款' };
      order.refundRequesting = true;
      order.refundRequester = params.role as 'buyer' | 'seller';
      console.log('[Mock] 退款申请已提交:', id);
      return { id, refundRequesting: true } as unknown as T;
    }

    // ── 同意退款 POST /v1/order/:id/refund-approve ──
    if (isPost && url.match(/\/order\/\d+\/refund-approve$/)) {
      const id = Number(url.match(/\/order\/(\d+)\/refund-approve$/)?.[1]);
      const order = mockOrderStore.find((o) => o.id === id);
      if (!order || !order.refundRequesting) throw { code: 400, msg: '无待处理的退款请求' };
      order.status = 6; order.statusDesc = '已退款';
      order.refundRequesting = false;
      console.log('[Mock] 退款已同意:', id);
      return { id, status: 6, statusDesc: '已退款' } as unknown as T;
    }

    // ── 取消 ──
    if (isPost && url.match(/\/order\/\d+\/cancel$/)) {
      const id = Number(url.match(/\/order\/(\d+)\/cancel$/)?.[1]);
      const order = mockOrderStore.find((o) => o.id === id);
      if (order) { order.status = 0; order.statusDesc = '已取消'; }
      return { id, status: 0, statusDesc: '已取消' } as unknown as T;
    }
    return { id: 1, orderNo: 'DB202607010001', status: 3, statusDesc: '服务中' } as unknown as T;
  }

  // ── 积分 ──
  if (url.includes('/points') || url.includes('/point')) {
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
  if (url.includes('/reviews/received') || url.includes('/reviews/mine') || url.includes('/reviews/pending')) {
    const params = (data || {}) as Record<string, unknown>;
    const pageNum = Number(params.pageNum) || 1;
    const pageSize = Number(params.pageSize) || 10;
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
      access_token: 'mock_token_xxx', refresh_token: 'mock_refresh_token_xxx',
      user: { id: MOCK_ME.id, nickname: MOCK_ME.nickname, avatar: '' },
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

export { request, BASE_URL };
