package com.dsevSport.DSEV_Sport.commerce.service;

import com.dsevSport.DSEV_Sport.commerce.dto.request.PaymentRequest;
import com.dsevSport.DSEV_Sport.commerce.dto.response.PaymentResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.UUID;

public interface PaymentService {
    PaymentResponse createVNPayPayment(PaymentRequest request, HttpServletRequest httpServletRequest);
    void handleVNPayReturn(HttpServletRequest request);
    PaymentResponse getPaymentByOrderId(UUID orderId);
    PaymentResponse getPaymentStatus(String transactionId);
}
