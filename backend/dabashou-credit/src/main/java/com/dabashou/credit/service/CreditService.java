package com.dabashou.credit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dabashou.common.core.PageResult;
import com.dabashou.credit.domain.Appeal;
import com.dabashou.credit.domain.Review;
import com.dabashou.credit.domain.Violation;
import com.dabashou.credit.dto.AppealDto;
import com.dabashou.credit.dto.ReviewDto;
import com.dabashou.credit.dto.ViolationDto;
import com.dabashou.credit.vo.AppealVo;
import com.dabashou.credit.vo.ReviewVo;
import com.dabashou.credit.vo.ViolationVo;

public interface CreditService extends IService<Review> {

    Long submitReview(Long userId, ReviewDto dto);

    PageResult<ReviewVo> getMyReviews(Long userId, int pageNum, int pageSize);

    PageResult<ReviewVo> getReceivedReviews(Long userId, int pageNum, int pageSize);

    ReviewVo getOrderReview(Long orderId);

    Long reportViolation(Long userId, ViolationDto dto);

    PageResult<ViolationVo> getMyViolations(Long userId, int pageNum, int pageSize);

    Long submitAppeal(Long userId, AppealDto dto);

    PageResult<AppealVo> getMyAppeals(Long userId, int pageNum, int pageSize);
}
