package com.ecommerce.ecommerceplatform.service;

import com.ecommerce.ecommerceplatform.cart.CartItem;
import com.ecommerce.ecommerceplatform.cart.CartItemRepository;
import com.ecommerce.ecommerceplatform.dto.OrderResponse;
import com.ecommerce.ecommerceplatform.order.*;
import com.ecommerce.ecommerceplatform.product.Product;
import com.ecommerce.ecommerceplatform.product.ProductRepository;
import com.ecommerce.ecommerceplatform.user.User;
import com.ecommerce.ecommerceplatform.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public OrderResponse checkout() {

        User user = getCurrentUser();

        List<CartItem> cartItems = cartItemRepository.findByUser(user);

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {

            Product product = cartItem.getProduct();

            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new RuntimeException(
                        "Insufficient stock for: " + product.getName()
                );
            }

            product.setStockQuantity(
                    product.getStockQuantity() - cartItem.getQuantity()
            );

            productRepository.save(product);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .productId(product.getId())
                    .productName(product.getName())
                    .priceAtPurchase(product.getPrice())
                    .quantity(cartItem.getQuantity())
                    .build();

            order.getItems().add(orderItem);

            total = total.add(
                    product.getPrice()
                            .multiply(BigDecimal.valueOf(cartItem.getQuantity()))
            );
        }

        order.setTotalAmount(total);

        Order savedOrder = orderRepository.save(order);

        cartItemRepository.deleteAll(cartItems);

        return toResponse(savedOrder);
    }

    public List<OrderResponse> getMyOrders() {

        return orderRepository
                .findByUserOrderByCreatedAtDesc(getCurrentUser())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private OrderResponse toResponse(Order order) {

        return OrderResponse.builder()
                .id(order.getId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .items(
                        order.getItems()
                                .stream()
                                .map(item ->
                                        OrderResponse.OrderItemResponse.builder()
                                                .productId(item.getProductId())
                                                .productName(item.getProductName())
                                                .priceAtPurchase(item.getPriceAtPurchase())
                                                .quantity(item.getQuantity())
                                                .build()
                                )
                                .toList()
                )
                .build();
    }
}
