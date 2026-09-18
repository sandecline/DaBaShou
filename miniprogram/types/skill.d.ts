export interface SkillCategory {
  id: number;
  name: string;
  icon: string;
  sortOrder: number;
  children?: SkillCategory[];
}

export interface SkillTagVo {
  id: number;
  categoryId: number;
  name: string;
  status: number;
}

export interface UserSkillVo {
  id: number;
  skillTagId: number;
  skillTagName: string;
  categoryName: string;
  proficiency: number;
  proficiencyDesc: string;
  description: string;
  createTime: string;
}

export type LocationType = 1 | 2 | 3;
export type Proficiency = 1 | 2 | 3 | 4;

export type { SkillTagVo as SkillTag };
export type { UserSkillVo as UserSkill };

export type { SkillShelf, SkillShelfForm, ShelfSearchParams, TimeSlot } from './shelf';

export interface PublishSkillParams {
  skillTagId: number;
  title: string;
  description: string;
  pointPrice: number;
  durationMinutes: number;
  locationType: LocationType;
}
