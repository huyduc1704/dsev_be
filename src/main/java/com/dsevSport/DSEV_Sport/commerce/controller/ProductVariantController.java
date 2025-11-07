package com.dsevSport.DSEV_Sport.commerce.controller;

import com.dsevSport.DSEV_Sport.commerce.dto.request.ProductVariantRequest;
import com.dsevSport.DSEV_Sport.commerce.dto.response.ApiResponse;
import com.dsevSport.DSEV_Sport.commerce.dto.response.ProductVariantResponse;
import com.dsevSport.DSEV_Sport.commerce.service.ProductVariantService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Product Variants", description = "Product Variant management endpoints")
@RestController
@RequestMapping("/api/v1/products/{productId}/variants")
@RequiredArgsConstructor
public class ProductVariantController {
    private final ProductVariantService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductVariantResponse>>> getVariantsByProduct(@PathVariable UUID productId) {
        return ResponseEntity.ok(
                ApiResponse.<List<ProductVariantResponse>>builder()
                        .data(service.getVariantsByProductId(productId))
                        .message("Variants retrieved successfully")
                        .code(200)
                        .build()
        );
    }

    @GetMapping("/{variantId}")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> getVariantById(
            @PathVariable UUID productId,
            @PathVariable UUID variantId) {
        // call service with variantId only (service signature expects single UUID)
        return ResponseEntity.ok(
                ApiResponse.<ProductVariantResponse>builder()
                        .data(service.getVariantById(variantId))
                        .message("Variant retrieved successfully")
                        .code(200)
                        .build()
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<ProductVariantResponse>> createVariant(
            @PathVariable UUID productId,
            @Valid @RequestBody ProductVariantRequest request) {
        request.setProductId(productId);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<ProductVariantResponse>builder()
                        .data(service.createVariant(request))
                        .message("Variant created successfully")
                        .code(201)
                        .build()
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @PutMapping("/{variantId}")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> updateVariant(
            @PathVariable UUID productId,
            @PathVariable UUID variantId,
            @Valid @RequestBody ProductVariantRequest request) {
        // call service with variantId only
        return ResponseEntity.ok(
                ApiResponse.<ProductVariantResponse>builder()
                        .data(service.updateVariant(variantId, request))
                        .message("Variant updated successfully")
                        .code(200)
                        .build()
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{variantId}")
    public ResponseEntity<Void> deleteVariant(
            @PathVariable UUID productId,
            @PathVariable UUID variantId) {
        // call service with variantId only
        service.deleteVariant(variantId);
        return ResponseEntity.noContent().build();
    }
}