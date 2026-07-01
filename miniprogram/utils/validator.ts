/**
 * 表单验证工具
 * 提供常用的校验函数
 */

interface ValidationRule {
  required?: boolean;
  message: string;
  validator: (value: unknown) => boolean;
}

interface ValidationResult {
  valid: boolean;
  message: string;
}

/**
 * 校验单个字段
 */
export function validateField(value: unknown, rules: ValidationRule[]): ValidationResult {
  for (const rule of rules) {
    if (rule.required && !value) {
      return { valid: false, message: rule.message };
    }
    if (value && !rule.validator(value)) {
      return { valid: false, message: rule.message };
    }
  }
  return { valid: true, message: '' };
}

// ===== 常用验证器 =====

/** 手机号 */
export function isPhone(value: string): boolean {
  return /^1[3-9]\d{9}$/.test(value);
}

/** 验证码（6位数字） */
export function isVerifyCode(value: string): boolean {
  return /^\d{6}$/.test(value);
}

/** 用户名（2-20位字母数字中文下划线） */
export function isUsername(value: string): boolean {
  return /^[\w\u4e00-\u9fa5]{2,20}$/.test(value);
}

/** 积分价格范围 */
export function isPointPrice(value: number): boolean {
  return Number.isInteger(value) && value >= 1 && value <= 9999;
}

/** 标题长度 */
export function isTitleValid(value: string): boolean {
  return value.trim().length >= 2 && value.trim().length <= 50;
}

/** 描述长度 */
export function isDescValid(value: string): boolean {
  return value.trim().length >= 5 && value.trim().length <= 500;
}

/** 评分范围 */
export function isRating(value: number): boolean {
  return Number.isInteger(value) && value >= 1 && value <= 5;
}
