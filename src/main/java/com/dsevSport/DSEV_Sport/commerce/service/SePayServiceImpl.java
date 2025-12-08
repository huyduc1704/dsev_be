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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class SePayServiceImpl implements SePayService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    // Accept common forms: ORD-123, ORD123, ORD_ABC, ORD1234567890
    private final Pattern ORDER_PATTERN = Pattern.compile("(ORD[-_]?\\d+|ORD[-_][A-Za-z0-9]+|ORD\\d+)", Pattern.CASE_INSENSITIVE);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void processWebhook(String rawJson) {
        Map<String, Object> payload = parseJsonSafe(rawJson);
        if (payload == null) {
            log.warn("Webhook payload invalid JSON, ignore");
            return;
        }
        log.info("Parsed payload: {}", payload);

        // SePay payload fields vary — we try multiple fallbacks
        String content = safeString(payload.get("content"));
        if (content == null) {
            content = safeString(payload.get("description"));
        }

        // transaction id fallback: prefer referenceCode -> id -> transactionId
        String transactionId = safeString(payload.get("referenceCode"));
        if (transactionId == null) transactionId = safeString(payload.get("id"));
        if (transactionId == null) transactionId = safeString(payload.get("transactionId"));

        // amount fallback: transferAmount or amount
        BigDecimal amount = parseAmount(payload.get("transferAmount"));
        if (amount == null) amount = parseAmount(payload.get("amount"));

        if (content == null) {
            log.warn("Webhook missing content/description; payload={}", payload);
            return;
        }
        if (amount == null) {
            log.warn("Webhook missing numeric amount; payload={}", payload);
            return;
        }

        // Extract order number
        String orderNumber = extractOrderNumber(content);
        if (orderNumber == null) {
            log.warn("Order number not found in content: {}", content);
            return;
        }
        log.info("Detected orderNumber = {}", orderNumber);

        // Lookup order; if not found, do NOT throw — log and return (SePay already got 200)
        var orderOpt = orderRepository.findByOrderNumber(orderNumber);
        if (orderOpt.isEmpty()) {
            log.warn("Order not found for orderNumber={} — payload ignored", orderNumber);
            return;
        }
        Order order = orderOpt.get();

        // already completed?
        if (order.getStatus() == OrderStatus.COMPLETED) {
            log.warn("Order {} already completed", orderNumber);
            return;
        }

        // price mismatch?
        if (order.getTotalPrice() == null) {
            log.warn("Order {} totalPrice is null", orderNumber);
            return;
        }
        if (order.getTotalPrice().compareTo(amount) != 0) {
            log.warn("Amount mismatch. Expected: {}, received {} — ignoring", order.getTotalPrice(), amount);
            return;
        }

        // prevent duplicates — if transactionId null, compose one
        if (transactionId == null || transactionId.isBlank()) {
            transactionId = "SEPAY_AUTO_" + System.currentTimeMillis();
            log.warn("TransactionId missing, generated fallback: {}", transactionId);
        }

        if (paymentRepository.existsByTransactionId(transactionId)) {
            log.warn("Duplicate transaction: {}", transactionId);
            return;
        }

        if (paymentRepository.existsByOrderId(order.getId())) {
            log.warn("Order {} already has payment record", orderNumber);
            return;
        }

        // mark order completed and create payment
        order.setStatus(OrderStatus.COMPLETED);
        order.setCompletedAt(LocalDateTime.now());
        orderRepository.save(order);

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(amount);
        payment.setPaymentMethod("SEPAY");
        payment.setTransactionId(transactionId);
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setCreatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        log.info("Order {} marked as PAID, transactionId={}", orderNumber, transactionId);
    }

    private Map<String, Object> parseJsonSafe(String json) {
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            log.warn("Invalid JSON payload: {}", e.getMessage());
            return null;
        }
    }

    private String safeString(Object o) {
        return o == null ? null : String.valueOf(o).trim();
    }

    private BigDecimal parseAmount(Object obj) {
        if (obj == null) return null;
        try {
            if (obj instanceof Number) {
                return new BigDecimal(((Number) obj).toString());
            } else {
                String s = String.valueOf(obj).trim();
                if (s.isEmpty()) return null;
                return new BigDecimal(s);
            }
        } catch (Exception e) {
            log.warn("Failed to parse amount from {}: {}", obj, e.getMessage());
            return null;
        }
    }

    private String extractOrderNumber(String content) {
        if (content == null) return null;
        Matcher m = ORDER_PATTERN.matcher(content);
        if (m.find()) {
            return m.group();
        }
        // fallback: try to find "ORD" followed by digits anywhere
        Pattern p2 = Pattern.compile("ORD\\d+", Pattern.CASE_INSENSITIVE);
        m = p2.matcher(content);
        return m.find() ? m.group() : null;
    }
}
