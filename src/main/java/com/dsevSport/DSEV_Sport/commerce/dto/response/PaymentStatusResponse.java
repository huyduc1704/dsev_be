package com.dsevSport.DSEV_Sport.commerce.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class PaymentStatusResponse {
    UUID orderId;
    String paymentStatus;
    String transactionId;
}
