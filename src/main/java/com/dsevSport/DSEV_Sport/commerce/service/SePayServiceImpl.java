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

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void processWebhook(String rawJson) {

        Map<String, Object> payload = parseJson(rawJson);

        log.info("Parsed payload: {}", payload);

        String content = (String) payload.get("content");
        String transactionId = String.valueOf(payload.get("referenceCode"));
        BigDecimal amount = new BigDecimal(payload.get("transferAmount").toString());

        if (content == null || transactionId == null) {
            log.warn("Missing content or transactionId");
            return;
        }

        // Extract Order
        String orderNumber = extractOrderNumber(content);
        log.info("Extracted Order Number: {}", orderNumber);

        if (orderNumber == null) {
            log.warn("Order number not found from content: {}", content);
            return;
        }

        // Find order
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElse(null);

        if (order == null) {
            log.warn("Order {} not found in DB", orderNumber);
            return;
        }

        // Check if completed
        if (order.getStatus() == OrderStatus.COMPLETED) {
            log.warn("Order {} already completed", orderNumber);
            return;
        }

        // Patch: Fix Amount Compare
        if (order.getTotalPrice().compareTo(amount) != 0) {
            log.warn("Amount mismatch for order {}: expected {}, got {}",
                    orderNumber, order.getTotalPrice(), amount);
            return;
        }

        // Duplicate?
        if (paymentRepository.existsByTransactionId(transactionId)) {
            log.warn("Duplicate transactionId: {}", transactionId);
            return;
        }

        // Already has payment?
        if (paymentRepository.existsByOrderId(order.getId())) {
            log.warn("Order {} already has a payment entry", orderNumber);
            return;
        }

        // Save order
        order.setStatus(OrderStatus.COMPLETED);
        order.setCompletedAt(LocalDateTime.now());
        orderRepository.save(order);

        // Create payment
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(amount);
        payment.setPaymentMethod("SEPAY");
        payment.setTransactionId(transactionId);
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setCreatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        log.info("Order {} marked as COMPLETED", orderNumber);
    }

    private Map<String, Object> parseJson(String json) {
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Invalid JSON payload", e);
        }
    }

    // THE FIX: ONLY MATCH ORD + digits
    private String extractOrderNumber(String content) {
        Pattern p = Pattern.compile("(ORD\\d+)");
        Matcher m = p.matcher(content);
        return m.find() ? m.group(1) : null;
    }
}

