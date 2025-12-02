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

    private final Pattern ORDER_PATTERN = Pattern.compile("(ORD-\\d+|ORD_[A-Za-z0-9]+)");
    @Override
    public void processWebhook(String rawJson) {
        Map<String, Object> payload = parseJson(rawJson);

        String content = (String) payload.get("content");
        Object transObj = payload.get("transactionId");

        if (transObj == null) {
            log.warn("Missing transactionId");
            return;
        }

        String transactionId = transObj.toString();

        BigDecimal amount = new BigDecimal(payload.get("transferAmount").toString());

        if (content == null || transactionId == null) {
            log.warn("Invalid webhook payload: {}", payload);
            return;
        }

        String orderNumber = extractOrderNumber(content);

        if (orderNumber == null) {
            log.warn("No order number found in content: {}", content);
            return;
        }

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() == OrderStatus.COMPLETED) {
            log.warn("Order {} already completed", orderNumber);
            return;
        }

        if (order.getTotalPrice().compareTo(amount) != 0) {
            log.warn("Amount mismatch. Expected: {}, received {}", order.getTotalPrice(), amount);
            return;
        }

        if (paymentRepository.existsByTransactionId(transactionId)) {
            log.warn("Duplicate transaction: {}", transactionId);
            return;
        }

        if (paymentRepository.existsByOrderId(order.getId())) {
            log.warn("Order {} already has payment", orderNumber);
            return;
        }

        order.setStatus(OrderStatus.COMPLETED);
        order.setCompletedAt(LocalDateTime.now());
        orderRepository.save(order);

        //create payment
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(amount);
        payment.setPaymentMethod("SEPAY");
        payment.setTransactionId(transactionId);
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setCreatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        log.info("Order {} paid successfully", orderNumber);
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
        return matcher.find() ? matcher.group() : null;
    }
}
