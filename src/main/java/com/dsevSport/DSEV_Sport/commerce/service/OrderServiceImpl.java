package com.dsevSport.DSEV_Sport.commerce.service;

import com.dsevSport.DSEV_Sport.commerce.dto.request.OrderRequest;
import com.dsevSport.DSEV_Sport.commerce.dto.response.OrderItemResponse;
import com.dsevSport.DSEV_Sport.commerce.dto.response.OrderResponse;
import com.dsevSport.DSEV_Sport.commerce.model.*;
import com.dsevSport.DSEV_Sport.commerce.repository.*;
import com.dsevSport.DSEV_Sport.common.util.enums.OrderStatus;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderServiceImpl implements OrderService {
    OrderRepository orderRepo;
    OrderItemRepository orderItemRepo;
    CartRepository cartRepo;
    ProductVariantRepository variantRepo;
    UserRepository userRepo;
    AddressRepository addressRepo;

    @Override
    public OrderResponse createOrderFromCart(String username, OrderRequest request) {
        // 1. Get User from username
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Validate Address belongs to User
        Address address = addressRepo.findById(request.getAddressId())
                .orElseThrow(() -> new RuntimeException("Address not found"));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Address does not belong to user");
        }

        // 3. Get Cart Items
        Cart cart = cartRepo.findByUser_Id(user.getId());
        if (cart == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // 4. Lock stock & calculate total
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (CartItem cartItem : cart.getItems()) {
            ProductVariant variant = cartItem.getProductVariant();

            if (variant.getStockQuantity() < cartItem.getQuantity()) {
                throw new RuntimeException("Insufficient stock for variant: " + variant.getId());
            }

            variant.setStockQuantity(variant.getStockQuantity() - cartItem.getQuantity());
            variantRepo.save(variant);

            totalPrice = totalPrice.add(cartItem.getUnitPrice().multiply(
                    BigDecimal.valueOf(cartItem.getQuantity())
            ));
        }

        // 5. Create order
        String orderNumber = "ORD" + System.currentTimeMillis();
        Order order = new Order();
        order.setUser(user);
        order.setAddress(address);
        order.setTotalPrice(totalPrice);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderNumber(orderNumber);
        order.setNote(request.getNote());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderRepo.save(order);

        // 6. Create order items
        List<OrderItemResponse> itemResponses = cart.getItems().stream()
                .map(cartItem -> {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(order);
                    orderItem.setProductVariant(cartItem.getProductVariant());
                    orderItem.setQuantity(cartItem.getQuantity());
                    orderItem.setUnitPrice(cartItem.getUnitPrice());
                    orderItem.setSubtotalPrice(cartItem.getUnitPrice().multiply(
                            BigDecimal.valueOf(cartItem.getQuantity())
                    ));
                    orderItem.setCreatedAt(LocalDateTime.now());
                    orderItemRepo.save(orderItem);

                    return toOrderItemResponse(orderItem);
                })
                .collect(Collectors.toList());

        // 7. Clear Cart
        cart.getItems().clear();
        cartRepo.save(cart);

        return toOrderResponse(order, address, itemResponses);
    }

    @Override
    public List<OrderResponse> getUserOrders(String username) {
        return orderRepo.findByUser_UsernameOrderByCreatedAtDesc(username).stream()
                .map(order -> {
                    Address address = order.getAddress();
                    List<OrderItemResponse> items = orderItemRepo.findByOrder_Id(order.getId())
                            .stream()
                            .map(this::toOrderItemResponse)
                            .collect(Collectors.toList());
                    return toOrderResponse(order, address, items);
                })
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponse updateOrderStatus(UUID orderId, OrderStatus status) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());

        if (status == OrderStatus.COMPLETED) {
            order.setCompletedAt(LocalDateTime.now());
        }

        orderRepo.save(order);

        List<OrderItemResponse> items = orderItemRepo.findByOrder_Id(orderId)
                .stream()
                .map(this::toOrderItemResponse)
                .collect(Collectors.toList());

        return toOrderResponse(order, order.getAddress(), items);
    }

    @Override
    public void cancelOrder(String username, UUID orderId) {
        // FIX: Validate user ownership
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Order does not belong to user");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Can only cancel pending orders");
        }

        // Restore stock
        List<OrderItem> items = orderItemRepo.findByOrder_Id(orderId);
        for (OrderItem item : items) {
            ProductVariant variant = item.getProductVariant();
            variant.setStockQuantity(variant.getStockQuantity() + item.getQuantity());
            variantRepo.save(variant);
        }

        order.setStatus(OrderStatus.CANCELED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepo.save(order);
    }

    private OrderResponse toOrderResponse(Order order, Address address, List<OrderItemResponse> items) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .note(order.getNote())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .completedAt(order.getCompletedAt())
                .fullName(address.getFullName())
                .phoneNumber(address.getPhoneNumber())
                .city(address.getCity())
                .ward(address.getWard())
                .street(address.getStreet())
                .items(items)
                .build();
    }

    private OrderItemResponse toOrderItemResponse(OrderItem item) {
        ProductVariant variant = item.getProductVariant();
        Product product = variant.getProduct();

        return OrderItemResponse.builder()
                .id(item.getId())
                .productName(product.getName())
                .productImage(
                        product.getImages().isEmpty()
                            ? null
                            :product.getImages().get(0).getImageUrl()
                )
                .color(variant.getColor())
                .size(variant.getSize())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotalPrice(item.getSubtotalPrice())
                .build();
    }
}