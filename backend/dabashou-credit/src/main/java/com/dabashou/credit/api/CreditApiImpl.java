package com.dabashou.credit.api;

import com.dabashou.user.api.UserApi;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CreditApiImpl implements CreditApi {

    private final UserApi userApi;

    public CreditApiImpl(UserApi userApi) {
        this.userApi = userApi;
    }

    @Override
    public void adjustTrustScore(Long userId, BigDecimal scoreChange, String type, Long orderId, String reason) {
        userApi.adjustTrustScore(userId, orderId, type, scoreChange, reason);
    }
}
