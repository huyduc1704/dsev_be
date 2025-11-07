package com.dsevSport.DSEV_Sport.commerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class ProductVariantResponse {
    UUID id;
    UUID productId;
    String color;
    String size;
    BigDecimal price;
    Integer stockQuantity;
}
