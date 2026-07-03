export interface TimeSlotVo {
  id: number;
  date?: string;
  dayOfWeek: number;
  startTime: string;
  endTime: string;
  available: boolean;
}

export interface ShelfItemVo {
  id: number;
  userId: number;
  nickname: string;
  avatar: string;
  trustScore: number;
  skillTagName: string;
  title: string;
  pointPrice: number;
  durationMinutes: number;
  locationType: number;
  status: number;
}

export interface ShelfDetailVo extends ShelfItemVo {
  description: string;
  locationTypeDesc: string;
  statusDesc: string;
  createTime: string;
}

export interface SkillShelfForm {
  skillTagId: number | null;
  title: string;
  description: string;
  pointPrice: number;
  durationMinutes: number;
  locationType: 1 | 2 | 3;
}

export interface ShelfSearchParams {
  keyword?: string;
  categoryId?: number;
  skillTagId?: number;
  locationType?: number;
  sortBy?: string;
  pageNum: number;
  pageSize: number;
}

export type ShelfStatus = 0 | 1 | 2;

export type { ShelfItemVo as SkillShelf };
export type { ShelfDetailVo as ShelfDetail };
export type { TimeSlotVo as TimeSlot };
