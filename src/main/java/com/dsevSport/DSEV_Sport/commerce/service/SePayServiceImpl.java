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

    // match ORD followed by digits (e.g. ORD1765...)
    private static final Pattern ORDER_PATTERN = Pattern.compile("ORD\\d+", Pattern.CASE_INSENSITIVE);

    /**
     * Use REQUIRES_NEW so webhook processing commits independently.
     * This prevents the webhook transaction from being rolled back if caller transaction exists.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void processWebhook(String rawJson) {
        Map<String, Object> payload = parseJsonSafe(rawJson);
        if (payload == null) {
            log.warn("Webhook payload invalid JSON, ignoring");
            return;
        }
        log.info("Parsed payload: {}", payload);

        // flexible field extraction
        String content = safeString(payload.get("content"));
        if (content == null) content = safeString(payload.get("description"));
        if (content == null) content = safeString(payload.get("note"));

        String transactionId = safeString(payload.get("referenceCode"));
        if (transactionId == null) transactionId = safeString(payload.get("id"));
        if (transactionId == null) transactionId = safeString(payload.get("transactionId"));

        BigDecimal amount = parseAmount(payload.get("transferAmount"));
        if (amount == null) amount = parseAmount(payload.get("amount"));

        if (content == null) {
            log.warn("Webhook missing content/description; payload={}", payload);
            return;
        }
        if (amount == null) {
            log.warn("Webhook missing amount; payload={}", payload);
            return;
        }

        // extract order number
        String orderNumber = extractOrderNumber(content);
        if (orderNumber == null) {
            log.warn("Order number not found in content: {}", content);
            return;
        }
        log.info("Detected orderNumber = {}", orderNumber);

        // lookup order
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

        // price check
        if (order.getTotalPrice() == null) {
            log.warn("Order {} totalPrice null — ignoring", orderNumber);
            return;
        }
        if (order.getTotalPrice().compareTo(amount) != 0) {
            log.warn("Amount mismatch. Expected: {}, received {} — ignoring", order.getTotalPrice(), amount);
            return;
        }

        // ensure transaction id
        if (transactionId == null || transactionId.isBlank()) {
            transactionId = "SEPAY_AUTO_" + System.currentTimeMillis();
            log.warn("TransactionId missing, generated {}", transactionId);
        }

        // duplicate checks
        if (paymentRepository.existsByTransactionId(transactionId)) {
            log.warn("Duplicate transaction: {}", transactionId);
            return;
        }
        if (paymentRepository.existsByOrderId(order.getId())) {
            log.warn("Order {} already has payment", orderNumber);
            return;
        }

        // mark order completed and create payment (within this new transaction)
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

        log.info("Order {} marked as PAID (transaction={})", orderNumber, transactionId);
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
        return m.find() ? m.group() : null;
    }
}
