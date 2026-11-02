package com.ecommerce.ecommerceplatform.controller;

import com.ecommerce.ecommerceplatform.dto.AddToCartRequest;
import com.ecommerce.ecommerceplatform.dto.CartItemResponse;
import com.ecommerce.ecommerceplatform.dto.UpdateCartRequest;
import com.ecommerce.ecommerceplatform.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;


    // Get logged-in user's cart
    @GetMapping
    public ResponseEntity<List<CartItemResponse>> getCart() {

        return ResponseEntity.ok(
                cartService.getCart()
        );
    }


    // Add product to cart
    @PostMapping
    public ResponseEntity<CartItemResponse> addToCart(
            @Valid @RequestBody AddToCartRequest request) {

        return ResponseEntity.ok(
                cartService.addToCart(request)
        );
    }


    // Update cart item quantity
    @PutMapping("/{id}")
    public ResponseEntity<CartItemResponse> updateQuantity(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCartRequest request) {

        return ResponseEntity.ok(
                cartService.updateQuantity(
                        id,
                        request.getQuantity()
                )
        );
    }


    // Remove item from cart
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeFromCart(
            @PathVariable Long id) {

        cartService.removeFromCart(id);

        return ResponseEntity.noContent().build();
    }
}
