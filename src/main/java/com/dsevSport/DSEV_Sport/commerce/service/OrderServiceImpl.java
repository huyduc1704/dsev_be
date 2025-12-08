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

        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Address address = addressRepo.findById(request.getAddressId())
                .orElseThrow(() -> new RuntimeException("Address not found"));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Address does not belong to user");
        }

        Cart cart = cartRepo.findByUser_Id(user.getId());
        if (cart == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        BigDecimal totalPrice = BigDecimal.ZERO;

        // LOCK STOCK
        for (CartItem c : cart.getItems()) {
            ProductVariant variant = c.getProductVariant();

            if (variant.getStockQuantity() < c.getQuantity()) {
                throw new RuntimeException("Insufficient stock for variant: " + variant.getId());
            }

            variant.setStockQuantity(variant.getStockQuantity() - c.getQuantity());
            variantRepo.save(variant);

            totalPrice = totalPrice.add(
                    c.getUnitPrice().multiply(BigDecimal.valueOf(c.getQuantity()))
            );
        }

        Order order = new Order();
        order.setUser(user);
        order.setAddress(address);
        order.setTotalPrice(totalPrice);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderNumber("ORD" + System.currentTimeMillis());
        order.setNote(request.getNote());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderRepo.save(order);

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
                }).toList();

        cart.getItems().clear();
        cartRepo.save(cart);

        return toOrderResponse(order, address, itemResponses);
    }

    @Override
    public List<OrderResponse> getUserOrders(String username) {
        return orderRepo.findByUser_UsernameOrderByCreatedAtDesc(username)
                .stream()
                .map(order -> {
                    List<OrderItemResponse> items = orderItemRepo.findByOrder_Id(order.getId())
                            .stream()
                            .map(this::toOrderItemResponse)
                            .toList();

                    return toOrderResponse(order, order.getAddress(), items);
                })
                .toList();
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
                .toList();

        return toOrderResponse(order, order.getAddress(), items);
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
            v.setStockQuantity(v.getStockQuantity() + i.getQuantity());
            variantRepo.save(v);
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
                        product.getImages() != null && !product.getImages().isEmpty()
                                ? product.getImages().get(0).getImageUrl()
                                : null
                )
                .color(variant.getColor())
                .size(variant.getSize())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotalPrice(item.getSubtotalPrice())
                .build();
    }
}