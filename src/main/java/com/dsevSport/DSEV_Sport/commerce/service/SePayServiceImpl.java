package com.dsevSport.DSEV_Sport.commerce.service;

import com.dsevSport.DSEV_Sport.commerce.model.Order;
import com.dsevSport.DSEV_Sport.commerce.model.Payment;
import com.dsevSport.DSEV_Sport.commerce.repository.OrderRepository;
import com.dsevSport.DSEV_Sport.commerce.repository.PaymentRepository;
import com.dsevSport.DSEV_Sport.common.util.enums.OrderStatus;
import com.dsevSport.DSEV_Sport.common.util.enums.PaymentStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class SePayServiceImpl implements SePayService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Pattern ORDER_PATTERN =
            Pattern.compile("(ORD[-_]?\\d+)", Pattern.CASE_INSENSITIVE);

    @Override
    @Transactional
    public void processWebhook(String rawJson) {

        Map<String, Object> payload = parseJsonSafe(rawJson);
        if (payload == null) {
            log.warn("❌ Invalid JSON");
            return;
        }

        log.info("📌 Webhook payload = {}", payload);

        String content = safe(payload.get("content"),
                payload.get("description"),
                payload.get("note"));

        String transactionId = safe(payload.get("referenceCode"),
                payload.get("id"),
                payload.get("transactionId"));

        BigDecimal amount = parseAmount(
                payload.get("transferAmount") != null
                        ? payload.get("transferAmount")
                        : payload.get("amount")
        );

        if (content == null || amount == null) {
            log.warn("❌ Missing content or amount");
            return;
        }

        // Extract order number
        String orderNumber = extractOrderNumber(content);
        if (orderNumber == null) {
            log.warn("❌ Cannot find order number inside content: {}", content);
            return;
        }

        log.info("🔍 Extracted orderNumber = {}", orderNumber);

        var orderOpt = orderRepository.findByOrderNumber(orderNumber);
        if (orderOpt.isEmpty()) {
            log.warn("❌ Order {} not found", orderNumber);
            return;
        }
        Order order = orderOpt.get();

        // Tolerance ±1000 VND
        BigDecimal orderPrice = order.getTotalPrice();
        BigDecimal diff = orderPrice.subtract(amount).abs();
        if (diff.compareTo(BigDecimal.valueOf(1000)) > 0) {
            log.warn("❌ Price mismatch. Order={}, Received={}", orderPrice, amount);
            return;
        }

        // Ensure transaction ID
        if (transactionId == null || transactionId.isBlank()) {
            transactionId = "SEPAY_" + System.currentTimeMillis();
            log.warn("⚠️ Missing transactionId -> Using generated {}", transactionId);
        }

        // Prevent duplicate
        if (paymentRepository.existsByTransactionId(transactionId)) {
            log.warn("⚠️ Duplicate transaction {}", transactionId);
            return;
        }

        // -------------------------------------
        // 🚀 FIX LOGIC QUAN TRỌNG NHẤT
        // -------------------------------------

        Optional<Payment> existingPaymentOpt = paymentRepository.findByOrderId(order.getId());

        if (existingPaymentOpt.isPresent()) {
            Payment existingPayment = existingPaymentOpt.get();

            log.info("🟡 Order {} already has a payment record. Updating...", orderNumber);

            // Update only if not SUCCESS
            if (existingPayment.getStatus() != PaymentStatus.SUCCESS) {
                existingPayment.setStatus(PaymentStatus.SUCCESS);
                existingPayment.setTransactionId(transactionId);
                existingPayment.setAmount(amount);
                paymentRepository.save(existingPayment);

                order.setStatus(OrderStatus.COMPLETED);
                order.setCompletedAt(LocalDateTime.now());
                orderRepository.save(order);

                log.info("🎉 Updated existing payment → SUCCESS");
            }

            return;
        }

        // -------------------------------------
        // 🟢 If no payment exists → create
        // -------------------------------------
        log.info("🟢 Creating new payment for order {}", orderNumber);

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(amount);
        payment.setTransactionId(transactionId);
        payment.setPaymentMethod("SEPAY");
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setCreatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // Update order
        order.setStatus(OrderStatus.COMPLETED);
        order.setCompletedAt(LocalDateTime.now());
        orderRepository.save(order);

        log.info("🎉 Payment created & order {} marked as COMPLETED", orderNumber);
    }

    // Helpers...
    private String safe(Object... values) {
        for (Object v : values) {
            if (v != null && !String.valueOf(v).trim().isEmpty()) {
                return String.valueOf(v).trim();
            }
        }
        return null;
    }

    private Map<String, Object> parseJsonSafe(String json) {
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            log.error("❌ JSON parse error: {}", e.getMessage());
            return null;
        }
    }

    private BigDecimal parseAmount(Object obj) {
        try {
            if (obj == null) return null;
            if (obj instanceof Number n) return new BigDecimal(n.toString());
            return new BigDecimal(obj.toString().replace(",", "").trim());
        } catch (Exception e) {
            log.warn("❌ Cannot parse amount: {}", obj);
            return null;
        }
    }

    private String extractOrderNumber(String content) {
        Matcher m = ORDER_PATTERN.matcher(content);
        return m.find() ? m.group() : null;
    }
}

