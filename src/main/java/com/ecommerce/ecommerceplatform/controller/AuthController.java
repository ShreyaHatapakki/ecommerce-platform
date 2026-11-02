package com.ecommerce.ecommerceplatform.controller;

import com.ecommerce.ecommerceplatform.dto.AuthResponse;
import com.ecommerce.ecommerceplatform.dto.LoginRequest;
import com.ecommerce.ecommerceplatform.dto.RegisterRequest;
import com.ecommerce.ecommerceplatform.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
