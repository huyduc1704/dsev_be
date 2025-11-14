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
import java.text.SimpleDateFormat;
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
        String txnRef = Optional.ofNullable(request.getOrderId()).orElse(UUID.randomUUID().toString());
        vnp_Params.put("vnp_TxnRef", txnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang: " + Optional.ofNullable(request.getOrderId()).orElse(""));
        vnp_Params.put("vnp_OrderType", vnpayConfig.getOrderType());
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnpayConfig.getReturnUrl());
        vnp_Params.put("vnp_IpAddr", VNPayUtil.getIpAddress(httpServletRequest));

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                try {
                    String encodedValue = URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.name());
                    hashData.append(fieldName).append('=').append(encodedValue);
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8.name()))
                            .append('=')
                            .append(encodedValue);
                    if (itr.hasNext()) {
                        query.append('&');
                        hashData.append('&');
                    }
                } catch (java.io.UnsupportedEncodingException e) {
                    throw new RuntimeException("Encoding error while building VNPay request", e);
                }
            }
        }

        String hashDataStr = hashData.toString();
        String queryUrl = query.toString();

        String vnp_SecureHash = VNPayUtil.hmacSHA512(vnpayConfig.getHashSecret(), hashDataStr);
        queryUrl += "&vnp_SecureHashType=SHA512&vnp_SecureHash=" + vnp_SecureHash;

        String paymentUrl = vnpayConfig.getUrl() + "?" + queryUrl;

        if (request.getOrderId() != null) {
            orderRepository.findByOrderNumber(request.getOrderId()).ifPresent(order -> {
                // avoid duplicate transaction_id constraint violation
                if (paymentRepository.findByTransactionId(txnRef).isEmpty()) {
                    BigDecimal amountDecimal = BigDecimal.ZERO;
                    String amtStr = vnp_Params.get("vnp_Amount");
                    if (amtStr != null && !amtStr.isEmpty()) {
                        try {
                            amountDecimal = new BigDecimal(amtStr).divide(new BigDecimal(100));
                        } catch (NumberFormatException ignored) { /* fallback to ZERO */ }
                    }
                    Payment prePayment = Payment.builder()
                            .order(order)
                            .transactionId(txnRef)
                            .amount(amountDecimal)
                            .paymentMethod("VNPAY")
                            .bankCode(null)
                            .status(PaymentStatus.PENDING)
                            .createdAt(LocalDateTime.now())
                            .build();
                    paymentRepository.save(prePayment);
                }
            });
        }

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

        BigDecimal amount = null;
        if (amountStr != null && !amountStr.isEmpty()) {
            amount = new BigDecimal(amountStr).divide(new BigDecimal(100));
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
