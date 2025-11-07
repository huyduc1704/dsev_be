package com.dsevSport.DSEV_Sport.commerce.controller;

import com.dsevSport.DSEV_Sport.commerce.dto.request.PaymentRequest;
import com.dsevSport.DSEV_Sport.commerce.dto.response.ApiResponse;
import com.dsevSport.DSEV_Sport.commerce.dto.response.PaymentResponse;
import com.dsevSport.DSEV_Sport.commerce.service.PaymentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Payments", description = "Payment processing endpoints")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/payments/vnpay")
    public ResponseEntity<ApiResponse<PaymentResponse>> createVNPayPayment(
            @RequestBody PaymentRequest request,
            HttpServletRequest httpServletRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<PaymentResponse>builder()
                        .message("Payment URL created successfully")
                        .data(paymentService.createVNPayPayment(request, httpServletRequest))
                        .code(201)
                        .build()
        );
    }

    @GetMapping("/payments/vnpay/return")
    public ResponseEntity<Void> handleVNPayReturn(HttpServletRequest request) {
        paymentService.handleVNPayReturn(request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/orders/{orderId}/payment")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByOrder(
            @PathVariable UUID orderId) {
        return ResponseEntity.ok(
                ApiResponse.<PaymentResponse>builder()
                        .message("Payment retrieved successfully")
                        .data(paymentService.getPaymentByOrderId(orderId))
                        .code(200)
                        .build()
        );
    }

    @GetMapping("/payments")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByTransactionId(
            @RequestParam String transactionId) {
        return ResponseEntity.ok(
                ApiResponse.<PaymentResponse>builder()
                        .message("Payment retrieved successfully")
                        .data(paymentService.getPaymentStatus(transactionId))
                        .code(200)
                        .build()
        );
    }
}
