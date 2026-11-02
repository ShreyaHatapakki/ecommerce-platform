package com.ecommerce.ecommerceplatform.service;

import com.ecommerce.ecommerceplatform.cart.CartItem;
import com.ecommerce.ecommerceplatform.cart.CartItemRepository;
import com.ecommerce.ecommerceplatform.dto.AddToCartRequest;
import com.ecommerce.ecommerceplatform.dto.CartItemResponse;
import com.ecommerce.ecommerceplatform.product.Product;
import com.ecommerce.ecommerceplatform.product.ProductRepository;
import com.ecommerce.ecommerceplatform.user.User;
import com.ecommerce.ecommerceplatform.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;


    // Converts CartItem entity into a safe response DTO
    private CartItemResponse toResponse(CartItem item) {

        return new CartItemResponse(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getProduct().getPrice(),
                item.getProduct().getImageUrl(),
                item.getQuantity()
        );
    }


    // Gets the currently logged-in user from the JWT
    private User getCurrentUser() {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }


    // Get the logged-in user's cart
    public List<CartItemResponse> getCart() {

        return cartItemRepository
                .findByUser(getCurrentUser())
                .stream()
                .map(this::toResponse)
                .toList();
    }


    // Add product to cart
    public CartItemResponse addToCart(AddToCartRequest request) {

        User user = getCurrentUser();

        Product product = productRepository
                .findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));


        // Check if product already exists in user's cart
        CartItem saved = cartItemRepository
                .findByUserAndProductId(user, request.getProductId())
                .map(existingItem -> {

                    // Increase existing quantity
                    existingItem.setQuantity(
                            existingItem.getQuantity()
                                    + request.getQuantity()
                    );

                    return cartItemRepository.save(existingItem);
                })
                .orElseGet(() -> {

                    // Create new cart item
                    return cartItemRepository.save(
                            CartItem.builder()
                                    .user(user)
                                    .product(product)
                                    .quantity(request.getQuantity())
                                    .build()
                    );
                });


        // Return safe DTO instead of CartItem entity
        return toResponse(saved);
    }


    // Update quantity of cart item
    public CartItemResponse updateQuantity(
            Long cartItemId,
            Integer quantity) {

        CartItem item = cartItemRepository
                .findById(cartItemId)
                .orElseThrow(() ->
                        new RuntimeException("Cart item not found"));


        // Make sure this cart item belongs to logged-in user
        if (!item.getUser().getId()
                .equals(getCurrentUser().getId())) {

            throw new RuntimeException(
                    "You do not have access to this cart item"
            );
        }


        // Update quantity
        item.setQuantity(quantity);

        CartItem saved = cartItemRepository.save(item);


        // Return safe DTO
        return toResponse(saved);
    }


    // Remove item from cart
    public void removeFromCart(Long cartItemId) {

        CartItem item = cartItemRepository
                .findById(cartItemId)
                .orElseThrow(() ->
                        new RuntimeException("Cart item not found"));


        // Make sure this cart item belongs to logged-in user
        if (!item.getUser().getId()
                .equals(getCurrentUser().getId())) {

            throw new RuntimeException(
                    "You do not have access to this cart item"
            );
        }


        // Delete cart item
        cartItemRepository.deleteById(cartItemId);
    }
}