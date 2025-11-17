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

        Calendar cld = Calendar.getInstance(); // server TZ = Asia/Ho_Chi_Minh
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

        String vnp_SecureHash = VNPayUtil.hmacSHA512(vnpayConfig.getHashSecret(), hashData.toString());
        String queryUrl = query + "&vnp_SecureHashType=SHA512&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = vnpayConfig.getUrl() + "?" + queryUrl;

        if (request.getOrderId() != null) {
            orderRepository.findById(request.getOrderId())
                    .ifPresentOrElse(order -> {
                        // compute amount first
                        BigDecimal computedAmount = BigDecimal.ZERO;
                        String amtStr = vnp_Params.get("vnp_Amount");
                        if (amtStr != null && !amtStr.isEmpty()) {
                            try {
                                computedAmount = new BigDecimal(amtStr).divide(new BigDecimal(100));
                            } catch (NumberFormatException ex) {
                                log.warn("Cannot parse vnp_Amount={}, default 0", amtStr, ex);
                            }
                        }
                        // make it final for lambda capture
                        final BigDecimal amountDecimal = computedAmount;

                        // nếu đã có payment theo txnRef thì không tạo lại
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
                    }, () -> {
                        log.warn("Order not found for orderNumber={}, skip pre-create payment", request.getOrderId());
                    });
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
        // 1) Verify secure hash from VNPay
        if (!verifyReturnSignature(request)) {
            log.warn("VNPay return signature invalid");
            return;
        }

        // 2) Read fields from return
        final String txnRef = request.getParameter("vnp_TxnRef");
        final String responseCode = request.getParameter("vnp_ResponseCode");
        final String transactionStatus = request.getParameter("vnp_TransactionStatus");
        String amountStr = request.getParameter("vnp_Amount");

        BigDecimal finalAmount = null;
        if (amountStr != null && !amountStr.isEmpty()) {
            try {
                finalAmount = new BigDecimal(amountStr).divide(new BigDecimal(100));
            } catch (NumberFormatException e) {
                log.warn("Invalid amount in return: {}", amountStr);
            }
        }

        // 3) Update payment + order atomically
        final BigDecimal amountForUpdate = finalAmount;

        paymentRepository.findByTransactionId(txnRef).ifPresentOrElse(payment -> {
            boolean success = isSuccess(responseCode, transactionStatus);

            // idempotent: only change when needed
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
            paymentRepository.save(payment);

            log.info("VNPay return processed: txnRef={}, responseCode={}, transStatus={}, status={}",
                    txnRef, responseCode, transactionStatus, payment.getStatus());
        }, () -> log.warn("Payment not found for txnRef={}, cannot update status", txnRef));
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

    private boolean verifyReturnSignature(HttpServletRequest request) {
        String secureHash = request.getParameter("vnp_SecureHash");
        if (secureHash == null) return false;

        Map<String, String[]> raw = request.getParameterMap();
        List<String> fieldNames = new ArrayList<>();
        Map<String, String> data = new HashMap<>();

        for (Map.Entry<String, String[]> e : raw.entrySet()) {
            String k = e.getKey();
            if ("vnp_SecureHash".equals(k) || "vnp_SecureHashType".equals(k)) continue;
            String[] vals = e.getValue();
            if (vals != null && vals.length > 0 && vals[0] != null && !vals[0].isEmpty()) {
                fieldNames.add(k);
                data.put(k, vals[0]);
            }
        }

        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        Iterator<String> it = fieldNames.iterator();
        while (it.hasNext()) {
            String name = it.next();
            String val = data.get(name);
            try {
                String encVal = URLEncoder.encode(val, StandardCharsets.UTF_8.name());
                hashData.append(name).append('=').append(encVal);
                if (it.hasNext()) hashData.append('&');
            } catch (Exception ex) {
                log.warn("Encoding error during signature verification", ex);
                return false;
            }
        }

        String calc = VNPayUtil.hmacSHA512(vnpayConfig.getHashSecret(), hashData.toString());
        return calc.equalsIgnoreCase(secureHash);
    }
}
