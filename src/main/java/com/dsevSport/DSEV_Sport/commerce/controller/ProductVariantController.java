package com.dsevSport.DSEV_Sport.commerce.controller;

import com.dsevSport.DSEV_Sport.commerce.dto.request.ProductVariantRequest;
import com.dsevSport.DSEV_Sport.commerce.dto.response.ApiResponse;
import com.dsevSport.DSEV_Sport.commerce.dto.response.ProductVariantResponse;
import com.dsevSport.DSEV_Sport.commerce.service.ProductVariantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/product-variants")
@RequiredArgsConstructor
public class ProductVariantController {
    private final ProductVariantService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductVariantResponse>>> getAllVariants() {
        return ResponseEntity.ok(
                ApiResponse.<List<ProductVariantResponse>>builder()
                        .data(service.getAllVariants())
                        .message("Variants retrieved successfully")
                        .success(true)
                        .build()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> getVariantById(@PathVariable UUID id) {
        return ResponseEntity.ok(
                ApiResponse.<ProductVariantResponse>builder()
                        .data(service.getVariantById(id))
                        .message("Variant retrieved successfully")
                        .success(true)
                        .build()
        );
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<ProductVariantResponse>>> getVariantsByProductId(@PathVariable UUID productId) {
        return ResponseEntity.ok(
                ApiResponse.<List<ProductVariantResponse>>builder()
                        .data(service.getVariantsByProductId(productId))
                        .message("Variants retrieved successfully")
                        .success(true)
                        .build()
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductVariantResponse>> createVariant(@Valid @RequestBody ProductVariantRequest request) {
        return ResponseEntity.ok(
                ApiResponse.<ProductVariantResponse>builder()
                        .data(service.createVariant(request))
                        .message("Variant created successfully")
                        .success(true)
                        .build()
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> updateVariant(@PathVariable UUID id, @Valid @RequestBody ProductVariantRequest request) {
        return ResponseEntity.ok(
                ApiResponse.<ProductVariantResponse>builder()
                        .data(service.updateVariant(id, request))
                        .message("Variant updated successfully")
                        .success(true)
                        .build()
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVariant(@PathVariable UUID id) {
        service.deleteVariant(id);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .message("Variant deleted successfully")
                        .success(true)
                        .build()
        );
    }
}