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
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class OrderServiceImpl implements OrderService {

    OrderRepository orderRepo;
    OrderItemRepository orderItemRepo;
    CartRepository cartRepo;
    ProductVariantRepository variantRepo;
    UserRepository userRepo;
    AddressRepository addressRepo;

    @Override
    public OrderResponse createOrderFromCart(String username, OrderRequest request) {
        // 1. Get user
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Address check
        Address address = null;
        if (request.getAddressId() != null) {
            address = addressRepo.findById(request.getAddressId())
                    .orElseThrow(() -> new RuntimeException("Address not found"));
            if (!address.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("Address does not belong to user");
            }
        }

        // 3. Cart
        Cart cart = cartRepo.findByUser_Id(user.getId());
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // 4. Lock stock & compute total
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (CartItem c : cart.getItems()) {
            ProductVariant variant = c.getProductVariant();
            if (variant == null) {
                throw new RuntimeException("Product variant not found for cart item");
            }
            if (variant.getStockQuantity() < c.getQuantity()) {
                throw new RuntimeException("Insufficient stock for variant: " + variant.getId());
            }
            // decrement stock
            variant.setStockQuantity(variant.getStockQuantity() - c.getQuantity());
            variantRepo.save(variant);

            totalPrice = totalPrice.add(
                    c.getUnitPrice().multiply(BigDecimal.valueOf(c.getQuantity()))
            );
        }

        // 5. Create order
        Order order = new Order();
        order.setUser(user);
        order.setAddress(address);
        order.setTotalPrice(totalPrice);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderNumber("ORD" + System.currentTimeMillis());
        order.setNote(request.getNote());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        // snapshot contact/address info
        order.setFullName(request.getFullName());
        order.setPhoneNumber(request.getPhoneNumber());
        order.setCity(request.getCity());
        order.setWard(request.getWard());
        order.setStreet(request.getStreet());

        orderRepo.save(order);

        // 6. Create order items
        List<OrderItemResponse> itemResponses = cart.getItems().stream()
                .map(c -> {
                    OrderItem item = new OrderItem();
                    item.setOrder(order);
                    item.setProductVariant(c.getProductVariant());
                    item.setQuantity(c.getQuantity());
                    item.setUnitPrice(c.getUnitPrice());
                    item.setSubtotalPrice(c.getUnitPrice().multiply(BigDecimal.valueOf(c.getQuantity())));
                    item.setCreatedAt(LocalDateTime.now());
                    orderItemRepo.save(item);
                    return toOrderItemResponse(item);
                })
                .collect(Collectors.toList());

        // 7. Clear cart
        cart.getItems().clear();
        cartRepo.save(cart);

        log.info("Order created: {}, user={}, total={}", order.getOrderNumber(), user.getId(), totalPrice);
        return toOrderResponse(order, itemResponses);
    }

    @Override
    public List<OrderResponse> getUserOrders(String username) {
        return orderRepo.findByUser_UsernameOrderByCreatedAtDesc(username)
                .stream()
                .map(order -> {
                    List<OrderItemResponse> items = orderItemRepo.findByOrder_Id(order.getId())
                            .stream()
                            .map(this::toOrderItemResponse)
                            .collect(Collectors.toList());
                    return toOrderResponse(order, items);
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

        log.info("Order {} status updated to {}", order.getOrderNumber(), status);
        return toOrderResponse(order, items);
    }

    @Override
    public void cancelOrder(String username, UUID orderId) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Order does not belong to user");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Only pending orders can be canceled");
        }

        List<OrderItem> items = orderItemRepo.findByOrder_Id(orderId);
        for (OrderItem i : items) {
            ProductVariant v = i.getProductVariant();
            if (v != null) {
                v.setStockQuantity(v.getStockQuantity() + i.getQuantity());
                variantRepo.save(v);
            }
        }

        order.setStatus(OrderStatus.CANCELED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepo.save(order);

        log.info("Order {} canceled by user {}", order.getOrderNumber(), username);
    }

    private OrderResponse toOrderResponse(Order order, List<OrderItemResponse> items) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .note(order.getNote())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .completedAt(order.getCompletedAt())
                .fullName(order.getFullName())
                .phoneNumber(order.getPhoneNumber())
                .city(order.getCity())
                .ward(order.getWard())
                .street(order.getStreet())
                .items(items)
                .build();
    }

    private OrderItemResponse toOrderItemResponse(OrderItem item) {
        ProductVariant variant = item.getProductVariant();
        Product product = variant != null ? variant.getProduct() : null;

        return OrderItemResponse.builder()
                .id(item.getId())
                .productName(product != null ? product.getName() : null)
                .productImage((product != null && product.getImages() != null && !product.getImages().isEmpty())
                        ? product.getImages().get(0).getImageUrl()
                        : null)
                .color(variant != null ? variant.getColor() : null)
                .size(variant != null ? variant.getSize() : null)
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotalPrice(item.getSubtotalPrice())
                .build();
    }
}
