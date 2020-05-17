package com.phonecompany.objects;

import java.math.BigDecimal;

public class PriceConfig {
    private BigDecimal basicPrice = BigDecimal.ONE;
    private BigDecimal loweredPrice = new BigDecimal("0.5");
    private BigDecimal longCallPrice = new BigDecimal("0.2");

    public BigDecimal getBasicPrice() {
        return basicPrice;
    }

    public BigDecimal getLoweredPrice() {
        return loweredPrice;
    }

    public BigDecimal getLongCallPrice() {
        return longCallPrice;
    }
}
