package com.dsevSport.DSEV_Sport.commerce.controller;

import com.dsevSport.DSEV_Sport.commerce.dto.request.AddressRequest;
import com.dsevSport.DSEV_Sport.commerce.dto.response.ApiResponse;
import com.dsevSport.DSEV_Sport.commerce.service.AddressService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Addresses", description = "Address management endpoints")
@RestController
@RequestMapping("/api/v1/me/addresses")
@RequiredArgsConstructor
public class AddressController {
    private final AddressService addressService;

    @GetMapping
    public ResponseEntity<ApiResponse<Object>> getMyAddresses(Authentication authentication) {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Addresses retrieved successfully")
                        .data(addressService.getAddresses(authentication.getName()))
                        .code(200)
                        .build()
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Object>> addAddress(
            Authentication authentication,
            @RequestBody AddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.builder()
                        .message("Address created successfully")
                        .data(addressService.addAddress(authentication.getName(), request))
                        .code(201)
                        .build()
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> updateAddress(
            Authentication authentication,
            @PathVariable UUID id,
            @RequestBody AddressRequest request) {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Address updated successfully")
                        .data(addressService.updateAddress(authentication.getName(), id, request))
                        .code(200)
                        .build()
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(
            Authentication authentication,
            @PathVariable UUID id) {
        addressService.deleteAddress(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
