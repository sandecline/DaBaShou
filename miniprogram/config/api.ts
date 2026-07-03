const ENV = 'dev' as const;

const CONFIG = {
  dev: {
    API_BASE_URL: 'http://127.0.0.1:9090/api',
    WS_BASE_URL: 'ws://127.0.0.1:9090/ws/chat',
  },
  prod: {
    API_BASE_URL: '',
    WS_BASE_URL: '',
  },
} as const;

export const API_BASE_URL = CONFIG[ENV].API_BASE_URL;
export const WS_BASE_URL = CONFIG[ENV].WS_BASE_URL;
export const USE_MOCK = false;
