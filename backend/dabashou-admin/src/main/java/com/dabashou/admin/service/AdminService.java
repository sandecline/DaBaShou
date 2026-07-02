package com.dabashou.admin.service;

import com.dabashou.admin.dto.AdminDto;
import com.dabashou.common.core.PageResult;

import java.util.Map;

public interface AdminService {

    PageResult<Map<String, Object>> listUsers(String keyword, Integer status, int pageNum, int pageSize);

    Map<String, Object> getUserDetail(Long id);

    void updateUserStatus(Long adminId, Long userId, Integer status);

    Map<String, Object> resetPassword(Long adminId, Long userId);

    PageResult<Map<String, Object>> listOrders(String keyword, Integer status, int pageNum, int pageSize);

    Map<String, Object> getOrderDetail(Long id);

    void arbitrateOrder(Long adminId, Long id, AdminDto.ArbitrateRequest request);

    PageResult<Map<String, Object>> listViolations(int pageNum, int pageSize);

    void handleViolation(Long adminId, Long id, AdminDto.ViolationHandleRequest request);

    PageResult<Map<String, Object>> listAppeals(int pageNum, int pageSize);

    void handleAppeal(Long adminId, Long id, AdminDto.AppealHandleRequest request);

    PageResult<Map<String, Object>> listReviews(int pageNum, int pageSize);

    void hideReview(Long adminId, Long id);

    PageResult<Map<String, Object>> listCampusAuths(Integer status, int pageNum, int pageSize);

    void reviewCampusAuth(Long adminId, Long id, AdminDto.CampusAuthReviewRequest request);

    Map<String, Object> getConfig();

    void updateConfig(Long adminId, Map<String, Object> config);
}
