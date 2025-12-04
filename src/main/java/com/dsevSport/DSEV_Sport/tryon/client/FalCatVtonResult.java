package com.dsevSport.DSEV_Sport.tryon.client;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FalCatVtonResult {
    String status;
    String outputImageUrl;
    String requestId;
    BigDecimal estimatedCost;
    String rawResponse;
}
