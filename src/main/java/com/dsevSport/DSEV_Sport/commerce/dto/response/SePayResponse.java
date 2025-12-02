package com.dsevSport.DSEV_Sport.commerce.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SePayResponse {
    UUID orderId;
    BigDecimal amount;
    String qrUrl;
}
