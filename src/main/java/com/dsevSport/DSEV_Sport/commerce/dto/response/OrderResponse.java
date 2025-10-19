package com.dsevSport.DSEV_Sport.commerce.dto.response;

import com.dsevSport.DSEV_Sport.common.util.enums.OrderStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderResponse {
    UUID id;
    String orderNumber;
    BigDecimal totalPrice;
    OrderStatus status;
    String note;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    LocalDateTime completedAt;

    // Address info
    String fullName;
    String phoneNumber;
    String city;
    String ward;
    String street;

    // Items
    List<OrderItemResponse> items;
}
