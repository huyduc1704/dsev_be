package com.dsevSport.DSEV_Sport.commerce.controller;

import com.dsevSport.DSEV_Sport.commerce.dto.request.AddToCartRequest;
import com.dsevSport.DSEV_Sport.commerce.dto.request.UpdateCartItemRequest;
import com.dsevSport.DSEV_Sport.commerce.dto.response.ApiResponse;
import com.dsevSport.DSEV_Sport.commerce.dto.response.CartResponse;
import com.dsevSport.DSEV_Sport.commerce.service.CartService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Cart", description = "Shopping cart management endpoints")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping("/me/cart")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(Authentication authentication) {
        return ResponseEntity.ok(
                ApiResponse.<CartResponse>builder()
                        .message("Get cart successfully")
                        .data(cartService.getCart(authentication.getName()))
                        .build()
        );
    }

    @PostMapping("/me/cart/items")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            Authentication authentication,
            @RequestBody AddToCartRequest request) {
        return ResponseEntity.ok(
                ApiResponse.<CartResponse>builder()
                        .message("Add to cart successfully")
                        .data(cartService.addToCart(authentication.getName(), request))
                        .code(200)
                        .build()
        );
    }

    @PatchMapping("/me/cart/items/{cartItemId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateCart(
            Authentication authentication,
            @PathVariable UUID cartItemId,
            @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(
                ApiResponse.<CartResponse>builder()
                        .message("Update cart item successfully")
                        .data(cartService.updateCartItem(authentication.getName(), cartItemId, request))
                        .code(200)
                        .build()
        );
    }

    @DeleteMapping("/me/cart/items/{cartItemId}")
    public ResponseEntity<Void> removeCartItem(
            Authentication authentication,
            @PathVariable UUID cartItemId) {
        cartService.removeCartItem(authentication.getName(), cartItemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me/cart/items")
    public ResponseEntity<Void> clearCart(Authentication authentication) {
        cartService.clearCart(authentication.getName());
        return ResponseEntity.noContent().build();
    }

}
