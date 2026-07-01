/**
 * 关于页
 * 展示应用基本信息、版本号、技术栈
 */

Page({
  data: {
    /** 应用名称 */
    appName: '搭把手',
    /** 版本号 */
    version: '1.0.0',
    /** 应用描述 */
    description: '校园技能共享互助平台，让知识在校园里流动起来。',
    /** 技术栈列表 */
    techStack: [
      { label: '前端框架', value: '微信原生 + TypeScript' },
      { label: 'UI 组件库', value: 'TDesign Miniprogram' },
      { label: '状态管理', value: 'App GlobalData' },
      { label: '后端服务', value: 'Spring Boot 微服务' },
      { label: '数据存储', value: 'MySQL + Redis' },
    ],
  },
});
