package com.dsevSport.DSEV_Sport.commerce.controller;

import com.dsevSport.DSEV_Sport.commerce.dto.request.SePayRequest;
import com.dsevSport.DSEV_Sport.commerce.dto.response.ApiResponse;
import com.dsevSport.DSEV_Sport.commerce.dto.response.PaymentStatusResponse;
import com.dsevSport.DSEV_Sport.commerce.dto.response.SePayResponse;
import com.dsevSport.DSEV_Sport.commerce.model.Order;
import com.dsevSport.DSEV_Sport.commerce.model.Payment;
import com.dsevSport.DSEV_Sport.commerce.repository.OrderRepository;
import com.dsevSport.DSEV_Sport.commerce.repository.PaymentRepository;
import com.dsevSport.DSEV_Sport.commerce.service.PaymentService;
import com.dsevSport.DSEV_Sport.common.config.SePayConfig;
import com.dsevSport.DSEV_Sport.common.security.OwnershipChecker;
import com.dsevSport.DSEV_Sport.common.util.SePayQrBuilder;
import com.dsevSport.DSEV_Sport.common.util.enums.OrderStatus;
import com.dsevSport.DSEV_Sport.common.util.enums.PaymentStatus;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
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
    private final OwnershipChecker ownershipChecker;

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

    @GetMapping("/payment/status")
    public ResponseEntity<ApiResponse<PaymentStatusResponse>> getPaymentStatus(
            @RequestParam UUID orderId,
            Principal principal
    ) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        String principalName = principal == null ? null : principal.getName();
        if (!ownershipChecker.isOwnerOrAdmin(order, principalName)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<PaymentStatusResponse>builder()
                            .code(403)
                            .message("You are not allowed to view this payment")
                            .build());
        }

        var status = paymentService.getPaymentStatus(orderId);

        return ResponseEntity.ok(
                ApiResponse.<PaymentStatusResponse>builder()
                        .code(200)
                        .data(status)
                        .message("Fetched payment status successfully")
                        .build()
        );
    }

}
