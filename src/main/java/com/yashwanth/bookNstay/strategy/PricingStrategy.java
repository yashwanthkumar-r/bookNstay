package com.yashwanth.bookNstay.strategy;

import com.yashwanth.bookNstay.entity.Inventory;

import java.math.BigDecimal;

public interface PricingStrategy {

    BigDecimal calculatePrice(Inventory inventory);
}
