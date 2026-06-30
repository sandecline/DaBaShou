import type { Plugin } from 'vite'

const mockUser = {
  id: 1, username: 'zhangsan', nickname: '张三', avatar: '', phone: '13800000000',
  email: 'zhangsan@test.com', pointBalance: 1000, trustScore: 4.5, longitude: 0, latitude: 0,
  campus: '仙林校区', building: '2号楼', bio: '热爱互助的大学生', status: 1,
  createTime: '2026-01-01 00:00:00', updateTime: '2026-01-01 00:00:00'
}
const mockLoginVo = {
  accessToken: 'mock_token_123456', refreshToken: 'mock_refresh_123456',
  expiresIn: 86400, userId: 1, nickname: '张三', avatar: ''
}
const mockSkillTags = [
  { id: 1, categoryId: 1, name: '高等数学辅导', status: 1 },
  { id: 2, categoryId: 1, name: '英语翻译', status: 1 },
  { id: 3, categoryId: 2, name: '电脑维修', status: 1 },
  { id: 4, categoryId: 2, name: '手机维修', status: 1 },
  { id: 5, categoryId: 3, name: 'PPT设计', status: 1 },
  { id: 6, categoryId: 3, name: '海报设计', status: 1 },
  { id: 7, categoryId: 4, name: '运动陪练', status: 1 },
  { id: 8, categoryId: 4, name: '音乐教学', status: 1 },
]
const mockCategories = [
  { id: 1, name: '学业辅导', icon: '📚', sortOrder: 1, children: [mockSkillTags[0], mockSkillTags[1]] },
  { id: 2, name: '维修帮忙', icon: '🔧', sortOrder: 2, children: [mockSkillTags[2], mockSkillTags[3]] },
  { id: 3, name: '设计美工', icon: '🎨', sortOrder: 3, children: [mockSkillTags[4], mockSkillTags[5]] },
  { id: 4, name: '运动陪练', icon: '🏃', sortOrder: 4, children: [mockSkillTags[6], mockSkillTags[7]] },
]
const mockShelves = Array.from({ length: 12 }, (_, i) => ({
  id: i + 1, userId: (i % 3) + 1, nickname: ['张三', '李四', '王五'][i % 3], avatar: '',
  trustScore: 3.5 + (i % 3) * 0.5, skillTagName: mockSkillTags[i % 8].name,
  title: `【${mockSkillTags[i % 8].name}】专业辅导，耐心解答`, pointPrice: 50 + i * 10,
  durationMinutes: 60, locationType: (i % 3) + 1, status: 1,
  description: '专业技能分享，欢迎预约', locationTypeDesc: '', statusDesc: '在售',
  createTime: '2026-06-01 10:00:00'
}))
const mockDemands = Array.from({ length: 8 }, (_, i) => ({
  id: i + 1, userId: (i % 3) + 1, nickname: ['张三', '李四', '王五'][i % 3], avatar: '',
  skillTagName: mockSkillTags[i % 8].name, title: `急求${mockSkillTags[i % 8].name}帮助`,
  pointReward: 80 + i * 20, deadline: '2026-07-15 18:00:00', locationType: 2,
  demandType: 1, demandTypeDesc: '普通需求', status: 1, statusDesc: '待接单',
  createTime: '2026-06-10 09:00:00', description: '急需帮助，请联系我'
}))
const mockOrders = Array.from({ length: 6 }, (_, i) => ({
  id: i + 1, orderNo: `ORD${String(20260601 + i).padStart(10, '0')}`,
  title: mockShelves[i].title, skillTagName: mockSkillTags[i % 8].name,
  pointAmount: mockShelves[i].pointPrice, status: [1, 2, 3, 4, 5, 7][i] as number,
  statusDesc: ['待支付', '已支付', '服务中', '待确认', '已完成', '争议中'][i],
  counterpartNickname: ['李四', '王五', '张三'][i % 3], counterpartAvatar: '',
  createTime: `2026-06-${10 + i} 14:00:00`
}))
const mockReviews = Array.from({ length: 4 }, (_, i) => ({
  id: i + 1, orderId: i + 1, orderNo: mockOrders[i].orderNo,
  reviewerId: (i % 3) + 1, reviewerNickname: ['张三', '李四', '王五'][i % 3],
  revieweeId: ((i + 1) % 3) + 1, revieweeNickname: ['李四', '王五', '张三'][i % 3],
  rating: 4 + (i % 2), content: ['服务很好！', '非常专业', '态度不错', '下次还找TA'][i],
  createTime: `2026-06-${15 + i} 10:00:00`
}))
const mockPointBalance = { available: 1000, frozen: 200, total: 1200 }
const mockPointTrans = Array.from({ length: 10 }, (_, i) => ({
  id: i + 1, type: i % 2 === 0 ? 1 : 2, typeDesc: i % 2 === 0 ? '收入' : '支出',
  amount: i % 2 === 0 ? 100 : -50, balanceAfter: 1000 + (i % 2 === 0 ? 100 : -50),
  orderId: null, orderNo: '', description: i % 2 === 0 ? '完成订单收入' : '预约服务支出',
  createTime: `2026-06-${1 + i} 12:00:00`
}))

function ok(data: any) { return { code: 200, msg: 'success', data } }
function page(list: any[], total?: number) {
  return ok({ total: total ?? list.length, list, records: list, pageNum: 1, pageSize: 20 })
}

function handleRoute(path: string, method: string): any {
  // ===== Auth =====
  if (path === '/v1/auth/login' && method === 'POST') return ok(mockLoginVo)
  if (path === '/v1/auth/register' && method === 'POST') return ok(null)
  if (path === '/v1/auth/logout' && method === 'POST') return ok(null)
  if (path === '/v1/auth/refresh' && method === 'POST') return ok(mockLoginVo)

  // ===== User =====
  if (path === '/v1/user/profile' && method === 'GET') return ok(mockUser)
  if (path === '/v1/user/profile' && method === 'PUT') return ok(null)
  if (path === '/v1/user/password' && method === 'PUT') return ok(null)
  if (path === '/v1/user/location' && method === 'PUT') return ok(null)
  if (path === '/v1/user/campus-auth' && method === 'GET') return ok({ id: 1, authType: 'student_id', studentNo: '20240001', realName: '张三', campus: '仙林校区', college: '计算机学院', status: 2, statusDesc: '已认证', createTime: '2026-01-15 10:00:00' })
  if (path === '/v1/user/campus-auth' && method === 'POST') return ok(null)
  if (path === '/v1/user/trust-score') return ok({ score: 4.5, level: 'gold', recentLogs: [] })
  if (/^\/v1\/users\/\d+$/.test(path) && method === 'GET') return ok(mockUser)

  // ===== Skills / Categories / Tags =====
  if (path === '/v1/skills/categories/tree') return ok(mockCategories)
  if (path.startsWith('/v1/skills/tags')) return ok(mockSkillTags)
  if (path === '/v1/categories/tree') return ok(mockCategories)
  if (path.startsWith('/v1/tags')) return ok(mockSkillTags)
  if (path === '/v1/users/me/skills') return ok([])

  // ===== Shelves =====
  if (path === '/v1/shelves' && method === 'GET') return page(mockShelves, 12)
  if (path === '/v1/shelves' && method === 'POST') return ok(1)
  if (path.startsWith('/v1/shelves/search')) return page(mockShelves, 12)
  if (path === '/v1/shelves/mine') return page(mockShelves.slice(0, 3))
  if (/^\/v1\/shelves\/\d+$/.test(path) && method === 'GET') return ok({ ...mockShelves[0], description: '专业技能分享，欢迎预约', locationTypeDesc: '线上' })
  if (/^\/v1\/shelves\/\d+$/.test(path) && method === 'PUT') return ok(null)
  if (/^\/v1\/shelves\/\d+$/.test(path) && method === 'DELETE') return ok(null)
  if (/^\/v1\/shelves\/\d+\/timeslots$/.test(path)) return ok([])
  if (/^\/v1\/users\/\d+\/shelves$/.test(path)) return page(mockShelves.slice(0, 3))

  // ===== Demands =====
  if (path === '/v1/demands' && method === 'GET') return page(mockDemands, 8)
  if (path === '/v1/demands' && method === 'POST') return ok(1)
  if (path.startsWith('/v1/demands/search')) return page(mockDemands, 8)
  if (path === '/v1/demands/mine') return page(mockDemands.slice(0, 2))
  if (/^\/v1\/demands\/\d+$/.test(path) && method === 'GET') return ok(mockDemands[0])
  if (/^\/v1\/demands\/\d+$/.test(path) && method === 'PUT') return ok(null)
  if (/^\/v1\/demands\/\d+$/.test(path) && method === 'DELETE') return ok(null)

  // ===== Orders (复数 /v1/orders) =====
  if (path === '/v1/orders' && method === 'GET') return page(mockOrders, 6)
  if (path === '/v1/orders' && method === 'POST') return ok(1)
  if (/^\/v1\/orders\/\d+$/.test(path) && method === 'GET') return ok({ ...mockOrders[0], buyerId: 1, buyerNickname: '张三', sellerId: 2, sellerNickname: '李四', verifyCode: 'ABC123', verifyCodeExpire: '2026-07-01 18:00:00' })

  // ===== Orders (单数 /v1/order) =====
  if (path === '/v1/order' && method === 'GET') return page(mockOrders, 6)
  if (path === '/v1/order' && method === 'POST') return ok(1)
  if (path === '/v1/order/from-shelf' && method === 'POST') return ok(1)
  if (path === '/v1/order/from-demand' && method === 'POST') return ok(1)
  if (/^\/v1\/order\/\d+$/.test(path) && method === 'GET') return ok({ ...mockOrders[0], buyerId: 1, buyerNickname: '张三', sellerId: 2, sellerNickname: '李四', verifyCode: 'ABC123', verifyCodeExpire: '2026-07-01 18:00:00' })
  if (/^\/v1\/order\/\d+\/status$/.test(path)) return ok({ status: mockOrders[0].status, statusDesc: mockOrders[0].statusDesc })
  if (/^\/v1\/order\/\d+\/pay$/.test(path)) return ok({ status: 2, verifyCode: 'XYZ789', verifyCodeExpire: '2026-07-01 18:00:00' })
  if (/^\/v1\/order\/\d+\/cancel$/.test(path)) return ok(null)
  if (/^\/v1\/order\/\d+\/start$/.test(path)) return ok(null)
  if (/^\/v1\/order\/\d+\/verify-code$/.test(path)) return ok({ verifyCode: 'NEW123', verifyCodeExpire: '2026-07-02 18:00:00' })
  if (/^\/v1\/order\/\d+\/verify$/.test(path)) return ok(null)
  if (/^\/v1\/order\/\d+\/confirm$/.test(path)) return ok(null)
  if (/^\/v1\/order\/\d+\/dispute$/.test(path)) return ok(null)
  if (/^\/v1\/order\/\d+\/arbitrate$/.test(path)) return ok(null)
  if (/^\/v1\/order\/\d+\/refund$/.test(path)) return ok(null)

  // ===== Points =====
  if (path === '/v1/points/balance') return ok(mockPointBalance)
  if (path === '/v1/points/transactions') return page(mockPointTrans)
  if (path === '/v1/points/stats') return ok({ totalIncome: 5000, totalExpense: 2000, totalReward: 500 })
  if (path === '/v1/points/sign-in' && method === 'POST') return ok({ reward: 10, consecutiveDays: 3 })
  if (path === '/v1/points/sign-in/status') return ok({ todaySigned: false, consecutiveDays: 2, reward: 10 })
  if (path === '/v1/points/guarantee-pool') return ok({ totalPool: 50000, myFrozen: 200 })

  // ===== Credit / Reviews =====
  if (path === '/v1/reviews' && method === 'POST') return ok(1)
  if (path === '/v1/reviews/mine' || path === '/v1/reviews/received') return page(mockReviews)
  if (path === '/v1/violations' && method === 'POST') return ok(1)
  if (path === '/v1/violations/mine') return page([])
  if (path === '/v1/appeals' && method === 'POST') return ok(1)
  if (path === '/v1/appeals/mine') return page([])

  // ===== Messages / Notifications =====
  if (path === '/v1/notifications/unread-count') return ok(3)
  if (path === '/v1/notifications') return page([])
  if (path === '/v1/notifications/read-all' && method === 'PUT') return ok(null)
  if (/^\/v1\/notifications\/\d+\/read$/.test(path)) return ok(null)
  if (/^\/v1\/notifications\/\d+$/.test(path) && method === 'DELETE') return ok(null)
  if (path === '/v1/chat/sessions') return page([])
  if (path.startsWith('/v1/chat/sessions') && path.endsWith('/messages')) return page([])
  if (path === '/v1/chat/send' && method === 'POST') return ok(null)

  // ===== Stats =====
  if (path === '/v1/stats/overview') return ok({ totalOrders: 28, completedOrders: 22, totalIncome: 1500, totalExpense: 800, trustScore: 4.5, skillCount: 5, reviewCount: 12, totalUsers: 150, totalShelves: 85, totalDemands: 45, todayNewUsers: 5, todayNewOrders: 12 })
  if (path === '/v1/stats/orders/trend') return ok(Array.from({ length: 7 }, (_, i) => ({ date: '2026-06-' + (20 + i), value: 3 + Math.floor(Math.random() * 5) })))
  if (path === '/v1/stats/points/trend') return ok(Array.from({ length: 7 }, (_, i) => ({ date: '2026-06-' + (20 + i), value: 50 + Math.floor(Math.random() * 30) })))
  if (path === '/v1/stats/skills/heat') return ok(mockSkillTags.slice(0, 5).map((t, i) => ({ skillTagId: t.id, skillTagName: t.name, shelfCount: 10 - i, demandCount: 8 - i, orderCount: 5 - i, heatScore: 90 - i * 10 })))
  if (path === '/v1/stats/categories') return ok([{ categoryName: '学业辅导', count: 15, percentage: 35 }, { categoryName: '维修帮忙', count: 10, percentage: 25 }, { categoryName: '设计美工', count: 8, percentage: 20 }, { categoryName: '运动陪练', count: 8, percentage: 20 }])

  // ===== Admin =====
  if (path.startsWith('/admin/')) {
    if (path.includes('/overview')) return ok({ totalUsers: 150, totalOrders: 320, totalShelves: 85, totalDemands: 45, todayNewUsers: 5, todayNewOrders: 12 })
    if (path.includes('/daily-trend')) return ok([])
    if (path.includes('/config')) return ok({ siteName: '搭把手', maxFileSize: 10 })
    if (path.includes('/users')) return page([mockUser])
    if (path.includes('/orders')) return page(mockOrders)
    if (path.includes('/campus-auths')) return page([])
    if (path.includes('/violations')) return page([])
    if (path.includes('/appeals')) return page([])
    if (path.includes('/reviews')) return page([])
    if (path.includes('/roles')) return page([])
    if (path.includes('/logs')) return page([])
    return ok(null)
  }

  // ===== File upload =====
  if (path === '/v1/files/upload' && method === 'POST') return ok({ id: 1, fileName: 'avatar.jpg', fileUrl: '/uploads/avatar.jpg', fileSize: 1024, fileType: 'image/jpeg' })

  return null
}

export function mockApiPlugin(): Plugin {
  return {
    name: 'mock-api',
    configureServer(server) {
      server.middlewares.use((req, res, next) => {
        if (!req.url?.startsWith('/api/')) return next()

        const path = req.url.replace('/api', '').split('?')[0]
        const method = req.method?.toUpperCase() || 'GET'

        // Simple approach: just respond immediately. For POST/PUT/DELETE
        // the mock doesn't need the body, so we let the stream handle itself.
        // If the client sends a body, it gets buffered by node and discarded.
        try {
          req.resume()
        } catch {}
        sendResponse(path, method)

        function sendResponse(p: string, m: string) {
          try {
            const result = handleRoute(p, m)
            if (result) {
              res.setHeader('Content-Type', 'application/json')
              res.end(JSON.stringify(result))
            } else {
              console.log(`[mock] Unhandled: ${m} ${p}`)
              res.setHeader('Content-Type', 'application/json')
              res.end(JSON.stringify(ok(null)))
            }
          } catch (err) {
            console.error(`[mock] Error handling ${m} ${p}:`, err)
            res.setHeader('Content-Type', 'application/json')
            res.end(JSON.stringify({ code: 500, msg: 'Internal mock error', data: null }))
          }
        }
      })
    }
  }
}


