package com.dsevSport.DSEV_Sport.commerce.service;

import com.dsevSport.DSEV_Sport.commerce.dto.request.PaymentRequest;
import com.dsevSport.DSEV_Sport.commerce.dto.response.PaymentResponse;
import com.dsevSport.DSEV_Sport.commerce.model.Order;
import com.dsevSport.DSEV_Sport.commerce.model.Payment;
import com.dsevSport.DSEV_Sport.commerce.repository.OrderRepository;
import com.dsevSport.DSEV_Sport.commerce.repository.PaymentRepository;
import com.dsevSport.DSEV_Sport.common.config.VNPayConfig;
import com.dsevSport.DSEV_Sport.common.util.VNPayUtil;
import com.dsevSport.DSEV_Sport.common.util.enums.OrderStatus;
import com.dsevSport.DSEV_Sport.common.util.enums.PaymentStatus;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final VNPayConfig vnpayConfig;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @Override
    public PaymentResponse createVNPayPayment(PaymentRequest request, HttpServletRequest httpServletRequest) {
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnpayConfig.getVersion());
        vnp_Params.put("vnp_Command", vnpayConfig.getCommand());
        vnp_Params.put("vnp_TmnCode", vnpayConfig.getTmnCode());
        if (request.getAmount() != null) {
            BigDecimal amt = BigDecimal.valueOf(request.getAmount());
            long amountInMinor = amt.multiply(BigDecimal.valueOf(100)).longValue();
            vnp_Params.put("vnp_Amount", String.valueOf(amountInMinor));
        }
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", request.getOrderId());
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang: " + request.getOrderId());
        vnp_Params.put("vnp_OrderType", vnpayConfig.getOrderType());
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnpayConfig.getReturnUrl());
        vnp_Params.put("vnp_IpAddr", VNPayUtil.getIpAddress(httpServletRequest));
        vnp_Params.put("vnp_CreateDate", new java.text.SimpleDateFormat("yyyyMMddHHmmss").format(new java.util.Date()));
        if (request.getBankCode() != null && !request.getBankCode().isEmpty()) {
            vnp_Params.put("vnp_BankCode", request.getBankCode());
        }

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

        StringJoiner hashJoiner = new StringJoiner("&");
        StringJoiner queryJoiner = new StringJoiner("&");
        for (String fieldName : fieldNames) {
            String value = vnp_Params.get(fieldName);
            if (value != null && value.length() > 0) {
                hashJoiner.add(fieldName + "=" + value); // raw for signing
                queryJoiner.add(URLEncoder.encode(fieldName, StandardCharsets.UTF_8)
                        + "=" + URLEncoder.encode(value, StandardCharsets.UTF_8)); // encoded for URL
            }
        }

        String hashData = hashJoiner.toString();
        String query = queryJoiner.toString();

        String secureHash = VNPayUtil.hmacSHA512(vnpayConfig.getHashSecret(), hashData);

        // append secureHashType and secureHash
        query += "&vnp_SecureHashType=" + URLEncoder.encode("SHA512", StandardCharsets.UTF_8)
                + "&vnp_SecureHash=" + secureHash;

        String paymentUrl = vnpayConfig.getUrl() + "?" + query;

        return PaymentResponse.builder()
                .code("00")
                .message("Success")
                .paymentUrl(paymentUrl)
                .build();
    }


    @Override
    public void handleVNPayReturn(HttpServletRequest request) {
        String orderNumber = request.getParameter("vnp_TxnRef");
        String responseCode = request.getParameter("vnp_ResponseCode");
        String transactionId = request.getParameter("vnp_TransactionNo");
        String bankCode = request.getParameter("vnp_BankCode");
        String amountStr = request.getParameter("vnp_Amount");

        java.math.BigDecimal amount = null;
        if (amountStr != null && !amountStr.isEmpty()) {
            amount = new java.math.BigDecimal(amountStr).divide(new java.math.BigDecimal(100));
        }

        Optional<Order> orderOpt = orderRepository.findByOrderNumber(orderNumber);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            if ("00".equals(responseCode)) {
                order.setStatus(OrderStatus.COMPLETED);
            } else {
                order.setStatus(OrderStatus.CANCELED);
            }
            orderRepository.save(order);

            Payment payment = Payment.builder()
                    .order(order)
                    .transactionId(transactionId)
                    .amount(amount)
                    .bankCode(bankCode)
                    .status("00".equals(responseCode) ? PaymentStatus.SUCCESS : PaymentStatus.FAILED)
                    .paymentMethod("VNPAY")
                    .createdAt(LocalDateTime.now())
                    .build();
            paymentRepository.save(payment);
        }
    }

    @Override
    public PaymentResponse getPaymentByOrderId(UUID orderId) {
        return paymentRepository.findByOrderId(orderId)
                .map(payment -> PaymentResponse.builder()
                        .code(payment.getStatus() != null ? payment.getStatus().name() : null)
                        .message("Payment found")
                        .paymentUrl(null)
                        .build())
                .orElse(PaymentResponse.builder()
                        .code("404")
                        .message("Payment not found")
                        .paymentUrl(null)
                        .build());
    }

    @Override
    public PaymentResponse getPaymentStatus(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId)
                .map(payment -> PaymentResponse.builder()
                        .code(payment.getStatus() != null ? payment.getStatus().name() : null)
                        .message("Payment status: " + (payment.getStatus() != null ? payment.getStatus().name() : "UNKNOWN"))
                        .paymentUrl(null)
                        .build())
                .orElse(PaymentResponse.builder()
                        .code("404")
                        .message("Payment not found")
                        .paymentUrl(null)
                        .build());
    }
}
