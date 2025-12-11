package com.dsevSport.DSEV_Sport.commerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class ProductResponse {
    UUID id;
    String name;
    String description;
    String brand;
    List<String> images;
    Boolean isActive;
    UUID categoryId;
    List<ProductVariantResponse> variants;
    List<TagResponse> tags;
}
