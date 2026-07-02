/**
 * Runtime API configuration for the WeChat miniprogram.
 *
 * DevTools can reach the local backend through 127.0.0.1. Real devices need
 * this host changed to a LAN IP or an HTTPS domain allowed in the WeChat
 * miniprogram backend.
 */
export const API_BASE_URL = 'http://127.0.0.1:9090/api';

export const WS_BASE_URL = 'ws://127.0.0.1:9090/ws/chat';

export const USE_MOCK = false;

