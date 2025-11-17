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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
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

        String base = request.getOrderId() != null ? request.getOrderId().toString().replace("-", "") : "";
        String rnd6 = VNPayUtil.getRandomNumber(6);
        String txnRef = (base.length() > 28 ? base.substring(0, 28) : base) + rnd6; // <=34
        vnp_Params.put("vnp_TxnRef", txnRef);

        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang: " + (request.getOrderId() != null ? request.getOrderId() : ""));
        vnp_Params.put("vnp_OrderType", vnpayConfig.getOrderType());
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnpayConfig.getReturnUrl());
        vnp_Params.put("vnp_IpAddr", VNPayUtil.getIpAddress(httpServletRequest));

        Calendar cld = Calendar.getInstance();
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
                    // IMPORTANT: hashData must be raw "key=value" WITHOUT URL-encoding
                    hashData.append(fieldName).append('=').append(fieldValue);

                    // query string for URL: encode values (names may be left raw or encoded; encoding values is required)
                    query.append(fieldName)
                            .append('=')
                            .append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.name()));

                    if (itr.hasNext()) {
                        query.append('&');
                        hashData.append('&');
                    }
                } catch (java.io.UnsupportedEncodingException e) {
                    throw new RuntimeException("Encoding error while building VNPay request", e);
                }
            }
        }

        String vnp_SecureHash = VNPayUtil.hmacSHA512(vnpayConfig.getHashSecret(), hashData.toString());
        String queryUrl = query + "&vnp_SecureHashType=SHA512&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = vnpayConfig.getUrl() + "?" + queryUrl;

        if (request.getOrderId() != null) {
            orderRepository.findById(request.getOrderId())
                    .ifPresentOrElse(order -> {
                        BigDecimal computedAmount = BigDecimal.ZERO;
                        String amtStr = vnp_Params.get("vnp_Amount");
                        if (amtStr != null && !amtStr.isEmpty()) {
                            try {
                                computedAmount = new BigDecimal(amtStr).divide(new BigDecimal(100));
                            } catch (NumberFormatException ex) {
                                log.warn("Cannot parse vnp_Amount={}, default 0", amtStr, ex);
                            }
                        }
                        final BigDecimal amountDecimal = computedAmount;

                        paymentRepository.findByTransactionId(txnRef).ifPresentOrElse(
                                existing -> log.debug("Payment already exists for txnRef={}", txnRef),
                                () -> {
                                    Payment prePayment = Payment.builder()
                                            .order(order)
                                            .transactionId(txnRef)
                                            .amount(amountDecimal)
                                            .paymentMethod("VNPAY")
                                            .status(PaymentStatus.PENDING)
                                            .createdAt(LocalDateTime.now())
                                            .build();
                                    log.debug("Saving Payment with txnRef={}, orderNumber={}", txnRef, order.getOrderNumber());
                                    paymentRepository.save(prePayment);
                                }
                        );
                    }, () -> log.warn("Order not found for orderNumber={}, skip pre-create payment", request.getOrderId()));
        } else {
            log.warn("request.orderId is null, skip pre-create payment");
        }

        log.info("VNPay paymentUrl = {}", paymentUrl);
        return PaymentResponse.builder()
                .code("00")
                .message("Success")
                .paymentUrl(paymentUrl)
                .build();
    }

    @Override
    @Transactional
    public void handleVNPayReturn(HttpServletRequest request) {
        if (!verifyReturnSignature(request)) {
            log.warn("VNPay return signature invalid");
            return;
        }

        final String txnRef = request.getParameter("vnp_TxnRef");
        final String responseCode = request.getParameter("vnp_ResponseCode");
        final String transactionStatus = request.getParameter("vnp_TransactionStatus");
        String amountStr = request.getParameter("vnp_Amount");
        final String bankCode = request.getParameter("vnp_BankCode");

        BigDecimal finalAmount = null;
        if (amountStr != null && !amountStr.isEmpty()) {
            try {
                finalAmount = new BigDecimal(amountStr).divide(new BigDecimal(100));
            } catch (NumberFormatException e) {
                log.warn("Invalid amount in return: {}", amountStr);
            }
        }

        final BigDecimal amountForUpdate = finalAmount;

        paymentRepository.findByTransactionId(txnRef).ifPresentOrElse(payment -> {
            boolean success = isSuccess(responseCode, transactionStatus);

            if (success && payment.getStatus() != PaymentStatus.SUCCESS) {
                payment.setStatus(PaymentStatus.SUCCESS);
                if (payment.getOrder() != null) {
                    payment.getOrder().setStatus(OrderStatus.COMPLETED);
                    payment.getOrder().setCompletedAt(LocalDateTime.now());
                    orderRepository.save(payment.getOrder());
                }
            } else if (!success && payment.getStatus() != PaymentStatus.SUCCESS) {
                payment.setStatus(PaymentStatus.FAILED);
                if (payment.getOrder() != null) {
                    payment.getOrder().setStatus(OrderStatus.CANCELED);
                    orderRepository.save(payment.getOrder());
                }
            }

            if (amountForUpdate != null) {
                payment.setAmount(amountForUpdate);
            }

            // save bank code from VNPay return
            if (bankCode != null && !bankCode.isEmpty()) {
                payment.setBankCode(bankCode);
            }

            paymentRepository.save(payment);

            log.info("VNPay return processed: txnRef={}, responseCode={}, transStatus={}, status={}",
                    txnRef, responseCode, transactionStatus, payment.getStatus());
        }, () -> log.warn("Payment not found for txnRef={}, cannot update status", txnRef));
    }

    private boolean verifyReturnSignature(HttpServletRequest request) {
        String secureHash = request.getParameter("vnp_SecureHash");
        if (secureHash == null) return false;

        String query = request.getQueryString(); // RAW FULL QUERY
        if (query == null) return false;

        // Split raw query into key=value parts and exclude secure hash fields
        List<String> params = new ArrayList<>();
        for (String part : query.split("&")) {
            if (part.startsWith("vnp_SecureHash")) continue;
            if (part.startsWith("vnp_SecureHashType")) continue;
            params.add(part);
        }

        // Sort by alphabet (key order) to match VNPay's signing process
        Collections.sort(params);

        // Build raw hashData (do NOT URL-decode or re-encode)
        StringBuilder hashData = new StringBuilder();
        for (int i = 0; i < params.size(); i++) {
            hashData.append(params.get(i));
            if (i < params.size() - 1) hashData.append("&");
        }

        String calc = VNPayUtil.hmacSHA512(vnpayConfig.getHashSecret(), hashData.toString());

        log.info("RAW QUERY = {}", query);
        log.info("RAW HASH DATA = {}", hashData);
        log.info("OUR CALC = {}", calc);
        log.info("VNPAY SECUREHASH = {}", secureHash);

        return secureHash.equalsIgnoreCase(calc);
    }

    private boolean isSuccess(String vnpResponseCode, String vnpTransactionStatus) {
        return "00".equals(vnpResponseCode) && (vnpTransactionStatus == null || "00".equals(vnpTransactionStatus));
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
