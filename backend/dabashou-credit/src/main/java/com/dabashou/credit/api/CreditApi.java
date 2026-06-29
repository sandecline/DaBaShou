package com.dabashou.credit.api;

import java.math.BigDecimal;

public interface CreditApi {

    void adjustTrustScore(Long userId, BigDecimal scoreChange, String type, Long orderId, String reason);
}
