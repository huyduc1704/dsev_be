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

        // ----- Extract flexible fields -----
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

        if (content == null) {
            log.warn("❌ Missing content");
            return;
        }

        if (amount == null) {
            log.warn("❌ Missing amount");
            return;
        }

        // ----- Extract order number -----
        String orderNumber = extractOrderNumber(content);
        if (orderNumber == null) {
            log.warn("❌ Order number not found in content: {}", content);
            return;
        }

        log.info("🔍 Extracted orderNumber = {}", orderNumber);

        // ----- Find order -----
        var orderOpt = orderRepository.findByOrderNumber(orderNumber);
        if (orderOpt.isEmpty()) {
            log.warn("❌ Order {} not found", orderNumber);
            return;
        }
        Order order = orderOpt.get();

        // ----- Check amount tolerance -----
        BigDecimal orderPrice = order.getTotalPrice();
        if (orderPrice == null) {
            log.warn("❌ Order price null");
            return;
        }

        BigDecimal diff = orderPrice.subtract(amount).abs();
        if (diff.compareTo(BigDecimal.valueOf(1000)) > 0) {
            log.warn("❌ Price mismatch. Expected {}, got {}", orderPrice, amount);
            return;
        }

        // ----- Ensure transaction id -----
        if (transactionId == null || transactionId.isBlank()) {
            transactionId = "SEPAY_" + System.currentTimeMillis();
            log.warn("⚠️ Missing transactionId -> Generated {}", transactionId);
        }

        // ----- Check duplicate payment -----
        if (paymentRepository.existsByTransactionId(transactionId)) {
            log.warn("⚠️ Duplicate transaction {}", transactionId);
            return;
        }

        // ----- Update order -----
        log.info("✅ Marking order {} as COMPLETED", orderNumber);

        order.setStatus(OrderStatus.COMPLETED);
        order.setCompletedAt(LocalDateTime.now());
        orderRepository.save(order);

        // ----- Save Payment -----
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(amount);
        payment.setTransactionId(transactionId);
        payment.setPaymentMethod("SEPAY");
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setCreatedAt(LocalDateTime.now());

        paymentRepository.save(payment);

        log.info("🎉 Order {} paid successfully with transaction {}", orderNumber, transactionId);
    }

    // ---------- Helpers ----------
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

            String s = obj.toString().replace(",", "").trim();
            return new BigDecimal(s);
        } catch (Exception e) {
            log.warn("❌ Cannot parse amount from {}", obj);
            return null;
        }
    }

    private String extractOrderNumber(String content) {
        Matcher m = ORDER_PATTERN.matcher(content);
        return m.find() ? m.group() : null;
    }
}

