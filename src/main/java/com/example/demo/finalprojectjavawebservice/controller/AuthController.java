package com.example.demo.finalprojectjavawebservice.controller;

import com.example.demo.finalprojectjavawebservice.dto.request.ChangePasswordRequest;
import com.example.demo.finalprojectjavawebservice.dto.request.ForgotPasswordRequest;
import com.example.demo.finalprojectjavawebservice.dto.request.LoginRequest;
import com.example.demo.finalprojectjavawebservice.dto.request.LogoutRequest;
import com.example.demo.finalprojectjavawebservice.dto.request.RefreshTokenRequest;
import com.example.demo.finalprojectjavawebservice.dto.request.RegisterRequest;
import com.example.demo.finalprojectjavawebservice.dto.request.ResetPasswordRequest;
import com.example.demo.finalprojectjavawebservice.dto.response.ApiResponse;
import com.example.demo.finalprojectjavawebservice.dto.response.AuthResponse;
import com.example.demo.finalprojectjavawebservice.dto.response.MessageResponse;
import com.example.demo.finalprojectjavawebservice.dto.response.PasswordResetResponse;
import com.example.demo.finalprojectjavawebservice.dto.response.UserResponse;
import com.example.demo.finalprojectjavawebservice.service.AuthService;
import com.example.demo.finalprojectjavawebservice.service.UserService;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok("Login successfully", authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Registered successfully", response));
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.ok("Token refreshed successfully", authService.refresh(request));
    }

    @PostMapping("/logout")
    public ApiResponse<MessageResponse> logout(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestBody(required = false) LogoutRequest request
    ) {
        authService.logout(authorizationHeader, request);
        return ApiResponse.ok("Logout successfully", new MessageResponse("Logout successfully"));
    }

    @PostMapping("/change-password")
    public ApiResponse<MessageResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Principal principal
    ) {
        return ApiResponse.ok("Password changed successfully", authService.changePassword(principal.getName(), request));
    }

    @PostMapping("/forgot-password")
    public ApiResponse<PasswordResetResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ApiResponse.ok("Password reset token generated successfully", authService.forgotPassword(request));
    }

    @PostMapping("/reset-password")
    public ApiResponse<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ApiResponse.ok("Password reset successfully", authService.resetPassword(request));
    }
}
