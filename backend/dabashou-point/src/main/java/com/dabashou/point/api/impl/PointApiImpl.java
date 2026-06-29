package com.dabashou.point.api.impl;

import com.dabashou.point.api.PointApi;
import com.dabashou.point.service.PointService;
import org.springframework.stereotype.Service;

@Service
public class PointApiImpl implements PointApi {

    private final PointService pointService;

    public PointApiImpl(PointService pointService) {
        this.pointService = pointService;
    }

    @Override
    public void freeze(Long userId, Long orderId, int amount, String description) {
        pointService.freeze(userId, orderId, amount, description);
    }

    @Override
    public void unfreezeAndTransfer(Long buyerId, Long sellerId, Long orderId, int amount, String description) {
        pointService.unfreezeAndTransfer(buyerId, sellerId, orderId, amount, description);
    }

    @Override
    public void refundFrozen(Long userId, Long orderId, int amount, String description) {
        pointService.refundFrozen(userId, orderId, amount, description);
    }

    @Override
    public void deduct(Long userId, int amount, String description) {
        pointService.deduct(userId, amount, description);
    }

    @Override
    public void reward(Long userId, int amount, String description) {
        pointService.reward(userId, amount, description);
    }
}
