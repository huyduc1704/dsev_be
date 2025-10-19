package com.dsevSport.DSEV_Sport.commerce.dto.response;

import com.dsevSport.DSEV_Sport.common.util.enums.PaymentStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VNPayCallbackResponse {
    String code;
    String message;
    String transactionId;
    String orderId;
    Double amount;
    String bankCode;
    String paymentMethod;
    PaymentStatus status;
}
