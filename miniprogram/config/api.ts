/**
 * Runtime API configuration for the WeChat miniprogram.
 *
 * 开发阶段默认指向本地后端；部署到生产环境前，请替换为真实 HTTPS 域名。
 */
export const API_BASE_URL = 'http://127.0.0.1:8080';

export const WS_BASE_URL = 'ws://127.0.0.1:9090/ws/chat';

/** TODO: 后端未就绪时启用 Mock 模式 */
export const USE_MOCK = true;



