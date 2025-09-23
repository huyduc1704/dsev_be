package com.dsevSport.DSEV_Sport.commerce.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductVariantRequest {
    @NotNull(message = "Product ID is required")
    UUID productId;

    String color;
    String size;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    BigDecimal price;

    @NotNull(message = "Stock quantity is required")
    @Positive(message = "Stock quantity must be positive")
    Integer stockQuantity;
}
