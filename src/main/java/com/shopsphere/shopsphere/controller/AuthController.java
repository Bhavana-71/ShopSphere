package com.shopsphere.shopsphere.controller;

import com.shopsphere.shopsphere.dto.AuthResponse;
import com.shopsphere.shopsphere.dto.LoginRequest;
import com.shopsphere.shopsphere.dto.RegisterRequest;
import com.shopsphere.shopsphere.entity.User;
import com.shopsphere.shopsphere.security.JwtService;
import com.shopsphere.shopsphere.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(request);
        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
        return ResponseEntity.status(HttpStatus.CREATED).body(
                AuthResponse.builder().token(token).email(user.getEmail()).role(user.getRole().name()).build()
        );
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = authService.login(request);
        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
        return ResponseEntity.ok(
                AuthResponse.builder().token(token).email(user.getEmail()).role(user.getRole().name()).build()
        );
    }
}