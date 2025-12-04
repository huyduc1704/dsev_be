package com.dsevSport.DSEV_Sport.commerce.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TryonSessionResponse {
    UUID id;

    UUID userId;
    UUID productImageId;
    UUID inputImageId;

    String imageInputUrl;
    String outputResultUrl;

    String status; // PROCESSING / COMPLETED / FAILED
    String deviceInfo;

    String modelName;
    String prompt;
    String requestId;

    BigDecimal estimatedCost;
    LocalDateTime createdAt;
    LocalDateTime finishedAt;
    String failReason;
    Integer retryCount;
}
