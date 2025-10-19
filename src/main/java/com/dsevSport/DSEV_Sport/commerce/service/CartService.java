package com.dsevSport.DSEV_Sport.commerce.service;

import com.dsevSport.DSEV_Sport.commerce.dto.request.AddToCartRequest;
import com.dsevSport.DSEV_Sport.commerce.dto.request.UpdateCartItemRequest;
import com.dsevSport.DSEV_Sport.commerce.dto.response.CartResponse;

import java.util.UUID;

public interface CartService {
    CartResponse getCart(String username);
    CartResponse addToCart(String username, AddToCartRequest request);
    CartResponse updateCartItem(String username, UUID cartItemId, UpdateCartItemRequest request);
    void removeCartItem(String username, UUID cartItemId);
    void clearCart(String username);
}
