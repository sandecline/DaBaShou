package com.dabashou.credit.service;

import com.dabashou.common.core.PageResult;
import com.dabashou.credit.dto.ReviewDto;
import com.dabashou.credit.vo.ReviewVo;

/**
 * 评价服务接口
 */
public interface ReviewService {

    /**
     * 提交评价
     */
    Long submit(Long userId, ReviewDto dto);

    /**
     * 我发出的评价（分页）
     */
    PageResult<ReviewVo> listMine(Long userId, int pageNum, int pageSize);

    /**
     * 我收到的评价（分页）
     */
    PageResult<ReviewVo> listReceived(Long userId, int pageNum, int pageSize);

    /**
     * 查询订单下当前用户的评价
     */
    ReviewVo getOrderReview(Long userId, Long orderId);
}
