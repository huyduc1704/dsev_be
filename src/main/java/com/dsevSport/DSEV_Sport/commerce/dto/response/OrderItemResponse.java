package com.dsevSport.DSEV_Sport.commerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor @NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class OrderItemResponse {
    UUID id;
    String productName;
    String productImage;
    String color;
    String size;
    Integer quantity;
    BigDecimal unitPrice;
    BigDecimal subtotalPrice;
}
