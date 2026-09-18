import type { UserProfile } from './user';

export interface IAppOption {
  globalData: {
    userInfo: UserProfile | null;
    token: string;
    isLoggedIn: boolean;
    unreadCount: number;
  };
  restoreSession(): void;
  clearSession(): void;
  clearMockJunk(): void;
}

export type TrustLevel = '新人' | '靠谱' | '金牌';
