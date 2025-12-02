package com.dsevSport.DSEV_Sport.commerce.controller;

import com.dsevSport.DSEV_Sport.commerce.dto.request.PaymentRequest;
import com.dsevSport.DSEV_Sport.commerce.dto.request.SePayRequest;
import com.dsevSport.DSEV_Sport.commerce.dto.response.ApiResponse;
import com.dsevSport.DSEV_Sport.commerce.dto.response.PaymentResponse;
import com.dsevSport.DSEV_Sport.commerce.dto.response.SePayResponse;
import com.dsevSport.DSEV_Sport.commerce.model.Order;
import com.dsevSport.DSEV_Sport.commerce.model.Payment;
import com.dsevSport.DSEV_Sport.commerce.repository.OrderRepository;
import com.dsevSport.DSEV_Sport.commerce.repository.PaymentRepository;
import com.dsevSport.DSEV_Sport.commerce.service.PaymentService;
import com.dsevSport.DSEV_Sport.common.config.SePayConfig;
import com.dsevSport.DSEV_Sport.common.util.SePayQrBuilder;
import com.dsevSport.DSEV_Sport.common.util.enums.OrderStatus;
import com.dsevSport.DSEV_Sport.common.util.enums.PaymentStatus;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Tag(name = "Payments", description = "Payment processing endpoints")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final SePayConfig config;

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

    @PostMapping("/sepay")
    public ResponseEntity<ApiResponse<SePayResponse>> createSePayQR(@RequestBody SePayRequest request) {

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Order already paid/canceled");
        }
        if (paymentRepository.existsByOrderId(order.getId())) {
            throw new RuntimeException("Payment already exists");
        }

        //generate transactionId
        String transactionId = "SEPAY_" + System.currentTimeMillis();

        //build QR content
        String content = order.getOrderNumber() + "|" + transactionId;

        //generate QR URL
        String qrUrl = SePayQrBuilder.buildQR(
                config.getBank(),
                config.getAccount(),
                order.getTotalPrice(),
                content
        );

        //Create payment pending
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getTotalPrice());
        payment.setTransactionId(transactionId);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaymentMethod("SEPAY");
        payment.setCreatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        SePayResponse response = SePayResponse.builder()
                .orderId(order.getId())
                .amount(order.getTotalPrice())
                .qrUrl(qrUrl)
                .build();

        return ResponseEntity.ok(
                ApiResponse.<SePayResponse>builder()
                        .message("Created payment successfully")
                        .code(200)
                        .data(response)
                        .build()
        );
    }

}
