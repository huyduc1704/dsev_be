package com.dsevSport.DSEV_Sport.commerce.service;

import com.dsevSport.DSEV_Sport.commerce.dto.request.AddToCartRequest;
import com.dsevSport.DSEV_Sport.commerce.dto.request.UpdateCartItemRequest;
import com.dsevSport.DSEV_Sport.commerce.dto.response.CartItemResponse;
import com.dsevSport.DSEV_Sport.commerce.dto.response.CartResponse;
import com.dsevSport.DSEV_Sport.commerce.model.Cart;
import com.dsevSport.DSEV_Sport.commerce.model.CartItem;
import com.dsevSport.DSEV_Sport.commerce.model.ProductVariant;
import com.dsevSport.DSEV_Sport.commerce.model.User;
import com.dsevSport.DSEV_Sport.commerce.repository.CartRepository;
import com.dsevSport.DSEV_Sport.commerce.repository.ProductRepository;
import com.dsevSport.DSEV_Sport.commerce.repository.ProductVariantRepository;
import com.dsevSport.DSEV_Sport.commerce.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartServiceImpl implements CartService{

    CartRepository cartRepo;
    UserRepository userRepo;
    ProductVariantRepository productRepo;


    @Override
    public CartResponse getCart(String username) {
        Cart cart = getOrCreateCart(username);
        return mapToCartResponse(cart);
    }


    @Override
    @Transactional
    public CartResponse addToCart(String username, AddToCartRequest request) {
        Cart cart = getOrCreateCart(username);
        ProductVariant variant = productRepo.findById(request.getProductVariantId())
                .orElseThrow(() -> new RuntimeException("Product variant not found"));

        // Check if item already exists
        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProductVariant().getId().equals(variant.getId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .productVariant(variant)
                    .quantity(request.getQuantity())
                    .unitPrice(variant.getPrice())
                    .build();
            cart.getItems().add(newItem);
        }

        cartRepo.save(cart);
        return mapToCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse updateCartItem(String username, UUID cartItemId, UpdateCartItemRequest request) {
        Cart cart = getOrCreateCart(username);
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
        item.setQuantity(request.getQuantity());
        cartRepo.save(cart);
        return mapToCartResponse(cart);
    }

    @Override
    @Transactional
    public void removeCartItem(String username, UUID cartItemId) {
        Cart cart = getOrCreateCart(username);
        cart.getItems().removeIf(item -> item.getId().equals(cartItemId));
        cartRepo.save(cart);
    }

    @Override
    @Transactional
    public void clearCart(String username) {
        Cart cart = getOrCreateCart(username);
        cart.getItems().clear();
        cartRepo.save(cart);
    }

    //helper
    private Cart getOrCreateCart(String username) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return cartRepo.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart newCart = Cart.builder().user(user).build();
                    return cartRepo.save(newCart);
                });
    }

    private CartResponse mapToCartResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(this::mapToCartItemResponse)
                .toList();

        BigDecimal total = items.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .id(cart.getId())
                .items(items)
                .totalPrice(total)
                .totalItems(cart.getItems().size())
                .build();
    }
    private CartItemResponse mapToCartItemResponse(CartItem item) {
        return CartItemResponse.builder()
                .id(item.getId())
                .productVariantId(item.getProductVariant().getId())
                .productName(item.getProductVariant().getProduct().getName())
                .color(item.getProductVariant().getColor())
                .size(item.getProductVariant().getSize())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(item.getSubtotal())
                .build();
    }
}
