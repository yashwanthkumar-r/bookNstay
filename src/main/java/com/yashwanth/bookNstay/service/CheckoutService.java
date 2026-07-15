package com.yashwanth.bookNstay.service;

import com.yashwanth.bookNstay.entity.Booking;

public interface CheckoutService {

    String getCheckoutSession(Booking booking, String successUrl, String failureUrl);
}
