package com.dsevSport.DSEV_Sport.commerce.controller;

import com.dsevSport.DSEV_Sport.commerce.dto.request.PaymentRequest;
import com.dsevSport.DSEV_Sport.commerce.dto.response.ApiResponse;
import com.dsevSport.DSEV_Sport.commerce.dto.response.PaymentResponse;
import com.dsevSport.DSEV_Sport.commerce.service.PaymentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
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
    @PermitAll
    @GetMapping(value = "/vnpay/return-app", produces = "text/html; charset=UTF-8")
    public String vnpayReturnApp(HttpServletRequest request) {
        paymentService.handleVNPayReturn(request);
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>VNPay Redirect</title></head><body>");
        html.append("<script>");
        html.append("const params = window.location.search;");
        html.append("const target = 'dsev://vnpay/callback' + params;");
        html.append("window.location.href = target;");
        html.append("</script>");
        html.append("<p>Đang chuyển hướng về ứng dụng...</p>");
        html.append("</body></html>");
        return html.toString();
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
