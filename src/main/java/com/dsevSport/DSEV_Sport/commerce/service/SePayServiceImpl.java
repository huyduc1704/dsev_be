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

    /**
     * Regex mới hỗ trợ tất cả các dạng sau:
     *  - ORD1765186035478
     *  - ORD-1765186035478
     *  - ORD_1765186035478
     */
    private final Pattern ORDER_PATTERN = Pattern.compile("ORD[-_]?([0-9]+)");

    @Override
    public void processWebhook(String rawJson) {

        Map<String, Object> payload = parseJson(rawJson);
        log.info("Parsed payload: {}", payload);

        String content = (String) payload.get("content");
        String transactionId = String.valueOf(payload.get("referenceCode"));   // SePay dùng referenceCode
        BigDecimal amount = new BigDecimal(payload.get("transferAmount").toString());

        if (content == null) {
            log.warn("Missing content in webhook");
            return;
        }

        // Extract Order Number
        String orderNumber = extractOrderNumber(content);
        if (orderNumber == null) {
            log.warn("Order number not found in content: {}", content);
            return;
        }

        log.info("Detected orderNumber = {}", orderNumber);

        // Find order by orderNumber (ví dụ: ORD1765186035478)
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderNumber));

        // Already paid?
        if (order.getStatus() == OrderStatus.COMPLETED) {
            log.warn("Order {} already completed", orderNumber);
            return;
        }

        // Check amount
        if (order.getTotalPrice().compareTo(amount) != 0) {
            log.warn("Amount mismatch for order {}: expected {}, actual {}",
                    orderNumber, order.getTotalPrice(), amount);
            return;
        }

        // Duplicate transaction
        if (paymentRepository.existsByTransactionId(transactionId)) {
            log.warn("Duplicate transaction: {}", transactionId);
            return;
        }

        // Already has payment
        if (paymentRepository.existsByOrderId(order.getId())) {
            log.warn("Order {} already linked to a payment", orderNumber);
            return;
        }

        // Update order status
        order.setStatus(OrderStatus.COMPLETED);
        order.setCompletedAt(LocalDateTime.now());
        orderRepository.save(order);

        // Create payment record
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(amount);
        payment.setPaymentMethod("SEPAY");
        payment.setTransactionId(transactionId);
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setCreatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        log.info("Order {} successfully marked as PAID", orderNumber);
    }

    private Map<String, Object> parseJson(String json) {
        try {
            return new ObjectMapper().readValue(json, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Invalid JSON payload", e);
        }
    }

    private String extractOrderNumber(String content) {
        Matcher matcher = ORDER_PATTERN.matcher(content);
        if (matcher.find()) {
            return "ORD" + matcher.group(1);
        }
        return null;
    }
}
