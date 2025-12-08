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

    private final Pattern ORDER_PATTERN =
            Pattern.compile("ORD[-_]?([A-Za-z0-9]+)", Pattern.CASE_INSENSITIVE);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void processWebhook(String rawJson) {

        Map<String, Object> payload = parseJsonSafe(rawJson);
        if (payload == null) {
            log.warn("Invalid JSON payload");
            return;
        }

        String content = safe(payload.get("content"));
        if (content == null) content = safe(payload.get("description"));

        String transactionId = safe(payload.get("referenceCode"));
        if (transactionId == null) transactionId = safe(payload.get("transactionId"));
        if (transactionId == null) transactionId = safe(payload.get("id"));

        BigDecimal amount = parseAmount(payload.get("transferAmount"));
        if (amount == null) amount = parseAmount(payload.get("amount"));

        if (content == null || amount == null) {
            log.warn("Payload missing fields: {}", payload);
            return;
        }

        String orderNumber = extractOrderNumber(content);
        if (orderNumber == null) {
            log.warn("Order number not found in content: {}", content);
            return;
        }

        var orderOpt = orderRepository.findByOrderNumber(orderNumber);
        if (orderOpt.isEmpty()) {
            log.warn("Order {} not found", orderNumber);
            return;
        }

        Order order = orderOpt.get();

        if (order.getStatus() == OrderStatus.COMPLETED) {
            log.warn("Order {} already completed", orderNumber);
            return;
        }

        if (order.getTotalPrice().compareTo(amount) != 0) {
            log.warn("Amount mismatch. expected {} received {}", order.getTotalPrice(), amount);
            return;
        }

        if (transactionId == null || transactionId.isBlank()) {
            transactionId = "SEPAY_" + System.currentTimeMillis();
        }

        if (paymentRepository.existsByTransactionId(transactionId)) {
            log.warn("Duplicate transaction {}", transactionId);
            return;
        }

        if (paymentRepository.existsByOrderId(order.getId())) {
            log.warn("Order {} already has payment", orderNumber);
            return;
        }

        order.setStatus(OrderStatus.COMPLETED);
        order.setCompletedAt(LocalDateTime.now());
        orderRepository.save(order);

        Payment p = new Payment();
        p.setOrder(order);
        p.setAmount(amount);
        p.setPaymentMethod("SEPAY");
        p.setTransactionId(transactionId);
        p.setStatus(PaymentStatus.SUCCESS);
        p.setCreatedAt(LocalDateTime.now());
        paymentRepository.save(p);

        log.info("Order {} marked as PAID, transaction {}", orderNumber, transactionId);
    }

    private Map<String, Object> parseJsonSafe(String json) {
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            return null;
        }
    }

    private String safe(Object o) {
        return o == null ? null : o.toString().trim();
    }

    private BigDecimal parseAmount(Object o) {
        try {
            if (o == null) return null;
            return new BigDecimal(o.toString().replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return null;
        }
    }

    private String extractOrderNumber(String content) {
        Matcher m = ORDER_PATTERN.matcher(content);
        if (!m.find()) return null;
        return "ORD" + m.group(1);
    }
}
