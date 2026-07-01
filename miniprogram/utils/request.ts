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
const MOCK_DELAY = 500;

/** Token 刷新最大重试次数，防止 401 → 刷新 → 401 的无限递归 */
const MAX_REFRESH_RETRIES = 3;

interface RequestOptions {
  url: string;
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE';
  data?: Record<string, unknown>;
  header?: Record<string, string>;
  showLoading?: boolean;
  loadingText?: string;
}

/**
 * 核心请求方法
 * 自动附加 JWT Token，401 时尝试刷新
 */
function request<T = unknown>(options: RequestOptions): Promise<ApiResponse<T>> {
  // Mock 模式：返回模拟数据
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
          // Token 过期，尝试刷新
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
        if (options.showLoading) {
          wx.hideLoading();
        }
      },
    });
  });
}

/**
 * Token 刷新 + 重试（带重试次数限制，防止无限递归 #12）
 */
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
      // 无 refresh token，触发重新登录
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
    _refreshRetryCount = 0; // 刷新成功，重置计数

    // 用新 token 重试原请求
    return request<T>(options);
  } catch (err) {
    _refreshRetryCount = 0;
    throw err;
  }
}

/**
 * 不自动刷新 Token 的原始请求（供 refreshTokenAndRetry 内部使用）
 */
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
        if (res.statusCode === 200) {
          resolve(res.data as ApiResponse<T>);
        } else {
          reject({ code: res.statusCode, msg: '刷新失败' });
        }
      },
      fail(err) {
        reject({ code: -1, msg: '网络错误', detail: err });
      },
    });
  });
}

// ===== Mock 模式（内存 Store，支持发布后可见） =====

/** Mock 当前用户 */
const MOCK_ME = {
  id: 1001,
  nickname: '张三',
  avatar: '',
  trustLevel: '靠谱' as const,
  trustScore: 3.5,
};

/** 技能分类 + 标签（供发布时反查分类名/标签名） */
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

/** 根据 skillTagId 查找标签 + 分类名 */
function findTag(tagId: number) {
  for (const cat of MOCK_CATEGORIES) {
    const tag = cat.tags.find((t) => t.id === tagId);
    if (tag) return { tag, categoryName: cat.name };
  }
  return { tag: { id: tagId, name: '未知', categoryId: 0 }, categoryName: '未知' };
}

/** 自增 ID */
let _mockId = 10000;
function nextId() { return ++_mockId; }

/** 内存技能列表（种子数据） */
const mockShelfStore: Record<string, unknown>[] = [
  {
    id: 1, title: 'Python 编程辅导', description: '帮助同学解决 Python 编程问题',
    pointPrice: 50, durationMinutes: 60, locationType: 1, images: [],
    status: 1, viewCount: 128, orderCount: 5, userId: 1001,
    skillTagId: 4, categoryName: '编程开发', tagName: 'Python',
    nickname: '张三', avatar: '', trustScore: 3.5,
    createTime: '2026-07-01 10:00:00',
  },
  {
    id: 2, title: '高数考前辅导', description: '期末高数复习，帮你理清重难点',
    pointPrice: 30, durationMinutes: 90, locationType: 3, images: [],
    status: 1, viewCount: 85, orderCount: 3, userId: 1002,
    skillTagId: 1, categoryName: '学业辅导', tagName: '数学',
    nickname: '李学霸', avatar: '', trustScore: 4.5,
    createTime: '2026-06-28 14:20:00',
  },
];

/** 内存需求列表（种子数据） */
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

/** 当前时间字符串 */
function nowStr() {
  const d = new Date();
  const p = (n: number) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`;
}

/**
 * 基于 method + url + data 生成模拟数据
 * 支持写入（POST 发布）和读取（GET 列表/详情），发布后数据持久存在于内存
 */
function getMockData<T>(options: RequestOptions): T {
  const { url, method, data } = options;
  const isPost = method === 'POST';
  const isGet = !method || method === 'GET';

  // ===== 技能分类 =====
  if (url.includes('/skills/categories')) {
    return MOCK_CATEGORIES as unknown as T;
  }

  // ===== 发布货架 POST /v1/shelves =====
  if (isPost && url.includes('/shelves') && !url.includes('/shelves/')) {
    const params = (data || {}) as Record<string, unknown>;
    const skillTagId = Number(params.skillTagId) || 0;
    const { tag, categoryName } = findTag(skillTagId);
    const id = nextId();
    const newSkill = {
      id,
      userId: MOCK_ME.id,
      skillTagId,
      title: String(params.title || ''),
      description: String(params.description || ''),
      pointPrice: Number(params.pointPrice) || 0,
      durationMinutes: Number(params.durationMinutes) || 60,
      locationType: Number(params.locationType) || 1,
      images: (params.images as string[]) || [],
      status: 1,
      viewCount: 0,
      orderCount: 0,
      categoryName,
      tagName: tag.name,
      nickname: MOCK_ME.nickname, avatar: '', trustScore: MOCK_ME.trustScore,
      createTime: nowStr(),
    };
    mockShelfStore.unshift(newSkill);
    console.log('[Mock] 新货架已发布:', newSkill.title, 'id=', id);
    return { id } as unknown as T;
  }

  // ===== 货架列表 GET /v1/shelves =====
  if (isGet && url.includes('/shelves') && !url.includes('/shelves/')) {
    const params = (data || {}) as Record<string, unknown>;
    let list = [...mockShelfStore];
    // 分类过滤
    const categoryId = Number(params.categoryId);
    if (categoryId) {
      list = list.filter((s) => {
        const tagId = s.skillTagId as number;
        if (!tagId) return false;
        const info = findTag(tagId);
        return info.tag.categoryId === categoryId;
      });
    }
    // 关键词过滤
    const keyword = params.keyword as string;
    if (keyword) {
      list = list.filter((s) => String(s.title).includes(keyword) || String(s.description).includes(keyword));
    }
    const pageNum = Number(params.pageNum) || 1;
    const pageSize = Number(params.pageSize) || 10;
    const start = (pageNum - 1) * pageSize;
    return {
      list: list.slice(start, start + pageSize),
      total: list.length,
      pageNum,
      pageSize,
    } as unknown as T;
  }

  // ===== 货架详情 GET /v1/shelves/:id =====
  if (isGet && url.match(/\/shelves\/\d+$/)) {
    const id = Number(url.match(/\/shelves\/(\d+)$/)?.[1]);
    const shelf = mockShelfStore.find((s) => s.id === id);
    return (shelf || mockShelfStore[0]) as unknown as T;
  }

  // ===== 发布需求 POST /v1/demands =====
  if (isPost && url.match(/\/demands\/?$/) && !url.includes('/demands/')) {
    const params = (data || {}) as Record<string, unknown>;
    const skillTagId = Number(params.skillTagId) || 0;
    const { tag } = findTag(skillTagId);
    const id = nextId();
    const newDemand = {
      id,
      userId: MOCK_ME.id,
      skillTagId,
      skillTagName: tag.name,
      title: String(params.title || ''),
      description: String(params.description || ''),
      pointReward: Number(params.pointReward) || 0,
      deadline: String(params.deadline || ''),
      demandType: Number(params.demandType) || 1,
      locationType: Number(params.locationType) || 1,
      images: (params.images as string[]) || [],
      status: 1,
      nickname: MOCK_ME.nickname, avatar: '', trustScore: MOCK_ME.trustScore,
      createTime: nowStr(),
    };
    mockDemandStore.unshift(newDemand);
    console.log('[Mock] 新需求已发布:', newDemand.title, 'id=', id);
    return { id } as unknown as T;
  }

  // ===== 需求列表 GET /v1/demands =====
  if (isGet && url.match(/\/demands\/?$/) && !url.includes('/demands/')) {
    const params = (data || {}) as Record<string, unknown>;
    let list = [...mockDemandStore];
    const keyword = params.keyword as string;
    if (keyword) {
      list = list.filter((d) => String(d.title).includes(keyword) || String(d.description).includes(keyword));
    }
    const pageNum = Number(params.pageNum) || 1;
    const pageSize = Number(params.pageSize) || 10;
    const start = (pageNum - 1) * pageSize;
    return {
      list: list.slice(start, start + pageSize),
      total: list.length,
      pageNum,
      pageSize,
    } as unknown as T;
  }

  // ===== 需求详情 GET /v1/demands/:id =====
  if (isGet && url.match(/\/demands\/\d+$/)) {
    const id = Number(url.match(/\/demands\/(\d+)$/)?.[1]);
    const demand = mockDemandStore.find((d) => d.id === id);
    return (demand || mockDemandStore[0]) as unknown as T;
  }

  // ===== 订单 =====
  if (url.includes('/order')) {
    if (url.includes('/orders?') || (isGet && url.match(/\/orders\/?$/))) {
      return {
        list: [{
          id: 1, orderNo: 'DB202607010001', title: 'Python 编程辅导',
          pointAmount: 50, status: 3, statusDesc: '服务中',
          buyerNickname: '李四', buyerAvatar: '',
          sellerNickname: '张三', sellerAvatar: '',
          counterpartNickname: '张三', counterpartAvatar: '',
          skillTagName: 'Python', durationMinutes: 60, createTime: '2026-07-01 10:00:00',
        }],
        total: 1, pageNum: 1, pageSize: 10,
      } as unknown as T;
    }
    // 订单详情
    return {
      id: 1, orderNo: 'DB202607010001', title: 'Python 编程辅导',
      pointAmount: 50, status: 3, statusDesc: '服务中', verifyCode: '1234',
      buyerNickname: '李四', buyerAvatar: '',
      sellerNickname: '张三', sellerAvatar: '',
      skillTagName: 'Python', durationMinutes: 60, remark: '',
      createTime: '2026-07-01 10:00:00', statusLogs: [],
    } as unknown as T;
  }
  if (url.includes('/point')) {
    return {
      available: 500,
      frozen: 50,
      total: 550,
      balance: 500,
    } as unknown as T;
  }
  // 会话列表 — /v1/chat/sessions
  if (url.includes('/chat/sessions') && !url.includes('/read') && !url.match(/\/sessions\/\d+/)) {
    return [
      {
        id: 1,
        otherUserId: 1001, otherNickname: '张三', otherAvatar: '',
        lastMessage: '好的，没问题！',
        lastTime: '2026-07-01 10:30:00',
        unreadCount: 2,
        orderId: 1,
        orderTitle: 'Python 编程辅导',
      },
    ] as unknown as T;
  }
  // 会话消息列表 — /v1/chat/messages (GET)
  if (url.includes('/chat/messages')) {
    return {
      list: [
        {
          id: 1,
          sessionId: 1,
          senderId: 1001,
          msgType: 1,
          content: '你好，请问可以帮我辅导 Python 吗？',
          isRead: 0 as 0 | 1,
          createTime: '2026-07-01 10:00:00',
        },
      ],
      total: 1, pageNum: 1, pageSize: 10,
    } as unknown as T;
  }
  if (url.includes('/user/profile')) {
    return {
      id: MOCK_ME.id,
      nickname: MOCK_ME.nickname,
      avatar: '',
      campus: '清水河校区',
      building: '七号楼',
      bio: '计算机学院学生，擅长 Python',
      trustLevel: MOCK_ME.trustLevel,
      trustScore: MOCK_ME.trustScore,
      pointBalance: 500,
      completedOrders: 5,
      publishedSkills: 3,
      positiveRate: 0.95,
    } as unknown as T;
  }
  if (url.includes('/credit')) {
    return {
      trustScore: 3.5,
      trustLevel: '靠谱',
      totalReviews: 8,
      positiveReviews: 7,
      neutralReviews: 1,
      negativeReviews: 0,
      logs: [],
    } as unknown as T;
  }
  if (url.includes('/auth')) {
    return {
      access_token: 'mock_token_xxx',
      refresh_token: 'mock_refresh_token_xxx',
      user: {
        id: MOCK_ME.id,
        nickname: MOCK_ME.nickname,
        avatar: '',
      },
    } as unknown as T;
  }

  // 列表类兜底
  return { list: [], total: 0, pageNum: 1, pageSize: 10 } as unknown as T;
}

function mockRequest<T>(options: RequestOptions): Promise<ApiResponse<T>> {
  console.log(`[Mock] ${options.method || 'GET'} ${options.url}`, options.data);

  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        code: 200,
        msg: 'success',
        data: getMockData<T>(options),
      });
    }, MOCK_DELAY);
  });
}

// ===== 便捷方法 =====

export const api = {
  get<T = unknown>(url: string, data?: Record<string, unknown>) {
    return request<T>({ url, method: 'GET', data });
  },
  post<T = unknown>(url: string, data?: Record<string, unknown>) {
    return request<T>({ url, method: 'POST', data });
  },
  put<T = unknown>(url: string, data?: Record<string, unknown>) {
    return request<T>({ url, method: 'PUT', data });
  },
  delete<T = unknown>(url: string, data?: Record<string, unknown>) {
    return request<T>({ url, method: 'DELETE', data });
  },
};

export { request, BASE_URL };
