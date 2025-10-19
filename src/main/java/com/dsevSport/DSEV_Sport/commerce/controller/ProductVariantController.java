package com.dsevSport.DSEV_Sport.commerce.controller;

import com.dsevSport.DSEV_Sport.commerce.dto.request.ProductVariantRequest;
import com.dsevSport.DSEV_Sport.commerce.dto.response.ApiResponse;
import com.dsevSport.DSEV_Sport.commerce.dto.response.ProductVariantResponse;
import com.dsevSport.DSEV_Sport.commerce.service.ProductVariantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")  // ✅ Đổi base path
@RequiredArgsConstructor
public class ProductVariantController {
    private final ProductVariantService service;

    // Admin: Quản lý tất cả variants
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/product-variants")
    public ResponseEntity<ApiResponse<List<ProductVariantResponse>>> getAllVariants() {
        return ResponseEntity.ok(
                ApiResponse.<List<ProductVariantResponse>>builder()
                        .data(service.getAllVariants())
                        .message("Variants retrieved successfully")
                        .code(200)
                        .build()
        );
    }

    // Public: Get variants của 1 product
    @GetMapping("/products/{productId}/variants")
    public ResponseEntity<ApiResponse<List<ProductVariantResponse>>> getVariantsByProduct(
            @PathVariable UUID productId) {
        return ResponseEntity.ok(
                ApiResponse.<List<ProductVariantResponse>>builder()
                        .data(service.getVariantsByProductId(productId))
                        .message("Variants retrieved successfully")
                        .code(200)
                        .build()
        );
    }

    // Public: Get 1 variant
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> getVariantById(
            @PathVariable UUID id) {
        return ResponseEntity.ok(
                ApiResponse.<ProductVariantResponse>builder()
                        .data(service.getVariantById(id))
                        .message("Variant retrieved successfully")
                        .code(200)
                        .build()
        );
    }

    // Admin: Create variant
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/products/{productId}/variants")
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

    // Admin: Update variant
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> updateVariant(
            @PathVariable UUID id,
            @Valid @RequestBody ProductVariantRequest request) {
        return ResponseEntity.ok(
                ApiResponse.<ProductVariantResponse>builder()
                        .data(service.updateVariant(id, request))
                        .message("Variant updated successfully")
                        .code(200)
                        .build()
        );
    }

    // Admin: Delete variant
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVariant(@PathVariable UUID id) {
        service.deleteVariant(id);
        return ResponseEntity.noContent().build();
    }
}
