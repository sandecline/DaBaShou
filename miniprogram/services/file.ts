/**
 * 文件上传服务
 * 后端统一文件上传端点: POST /v1/files/upload
 * 与 docs/api/system.md 保持一致
 */

import { BASE_URL } from '../utils/request';

export const fileService = {
  /** 上传单个文件，返回远程 URL */
  upload(filePath: string, fileName = 'file'): Promise<string> {
    const token = wx.getStorageSync('dabashou_token');
    return new Promise((resolve, reject) => {
      wx.uploadFile({
        url: `${BASE_URL}/v1/files/upload`,
        filePath,
        name: fileName,
        header: { Authorization: `Bearer ${token}` },
        success(res) {
          try {
            const data = JSON.parse(res.data);
            if (data.code === 200 && data.data) {
              const url = typeof data.data === 'string' ? data.data : (data.data as { url?: string }).url;
              if (url) resolve(url);
              else reject(data);
            }
            else reject(data);
          } catch { reject(res.data); }
        },
        fail: reject,
      });
    });
  },

  /** 批量上传，返回远程 URL 列表 */
  async uploadBatch(filePaths: string[]): Promise<string[]> {
    if (filePaths.length === 0) return [];
    const results = await Promise.allSettled(
      filePaths.map((fp) => this.upload(fp))
    );
    return results
      .filter((r): r is PromiseFulfilledResult<string> => r.status === 'fulfilled')
      .map((r) => r.value)
      .filter(Boolean);
  },
};
