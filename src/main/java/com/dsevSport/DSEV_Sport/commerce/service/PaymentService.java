package com.dsevSport.DSEV_Sport.commerce.service;

import com.dsevSport.DSEV_Sport.commerce.dto.response.PaymentStatusResponse;


import java.util.UUID;

public interface PaymentService {
    PaymentStatusResponse getPaymentStatus(UUID orderId);
}
