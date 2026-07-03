export interface UserBrief {
  id: number;
  nickname: string;
  avatar: string;
  trustScore: number;
  trustLevel: string;
}

export interface UserProfile {
  id: number;
  username: string;
  nickname: string;
  avatar: string;
  phone: string;
  email: string;
  pointBalance: number;
  trustScore: number;
  trustLevel: string;
  longitude: number;
  latitude: number;
  campus: string;
  building: string;
  bio: string;
  status: number;
  createTime: string;
}

export interface UpdateProfileParams {
  nickname?: string;
  avatar?: string;
  campus?: string;
  building?: string;
  bio?: string;
}

export interface CampusAuthVo {
  id: number;
  authType: string;
  studentNo: string;
  realName: string;
  campus: string;
  college: string;
  status: number;
  statusDesc: string;
  reviewRemark: string | null;
  reviewTime: string | null;
  createTime: string;
}

export interface TrustScoreVo {
  score: number;
  level: string;
  recentLogs: TrustLogItem[];
}

export interface TrustLogItem {
  type: string;
  scoreChange: number;
  scoreBefore: number;
  scoreAfter: number;
  reason: string;
  createTime: string;
}
