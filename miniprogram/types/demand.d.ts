export type DemandType = 1 | 2;
export type DemandStatus = 0 | 1 | 2 | 3;

export interface DemandItemVo {
  id: number;
  userId: number;
  nickname: string;
  avatar: string;
  skillTagName: string;
  title: string;
  pointReward: number;
  deadline: string | null;
  locationType: number;
  demandType: DemandType;
  status: DemandStatus;
  createTime: string;
}

export interface DemandDetailVo extends DemandItemVo {
  description: string;
  demandTypeDesc: string;
  statusDesc: string;
  longitude: number;
  latitude: number;
  campus: string;
  building: string;
}

export interface PublishDemandParams {
  skillTagId: number | null;
  title: string;
  description?: string;
  pointReward?: number;
  deadline?: string;
  locationType: number;
  demandType?: DemandType;
  longitude?: number;
  latitude?: number;
}

export interface DemandSearchParams {
  keyword?: string;
  categoryId?: number;
  skillTagId?: number;
  demandType?: DemandType;
  status?: DemandStatus;
  sortBy?: string;
  pageNum: number;
  pageSize: number;
}

export type { DemandItemVo as Demand };
export type { DemandDetailVo as DemandDetail };
