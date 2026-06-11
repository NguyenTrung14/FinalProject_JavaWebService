package com.example.demo.finalprojectjavawebservice.service;

import com.example.demo.finalprojectjavawebservice.dto.request.ChangePasswordRequest;
import com.example.demo.finalprojectjavawebservice.dto.request.ForgotPasswordRequest;
import com.example.demo.finalprojectjavawebservice.dto.request.LoginRequest;
import com.example.demo.finalprojectjavawebservice.dto.request.LogoutRequest;
import com.example.demo.finalprojectjavawebservice.dto.request.RefreshTokenRequest;
import com.example.demo.finalprojectjavawebservice.dto.request.ResetPasswordRequest;
import com.example.demo.finalprojectjavawebservice.dto.response.AuthResponse;
import com.example.demo.finalprojectjavawebservice.dto.response.MessageResponse;
import com.example.demo.finalprojectjavawebservice.dto.response.PasswordResetResponse;
import com.example.demo.finalprojectjavawebservice.dto.response.UserResponse;
import com.example.demo.finalprojectjavawebservice.entity.PasswordResetToken;
import com.example.demo.finalprojectjavawebservice.entity.RefreshToken;
import com.example.demo.finalprojectjavawebservice.entity.UserAccount;
import com.example.demo.finalprojectjavawebservice.entity.enums.AccountStatus;
import com.example.demo.finalprojectjavawebservice.exception.BadRequestException;
import com.example.demo.finalprojectjavawebservice.exception.ResourceNotFoundException;
import com.example.demo.finalprojectjavawebservice.exception.UnauthorizedException;
import com.example.demo.finalprojectjavawebservice.repository.PasswordResetTokenRepository;
import com.example.demo.finalprojectjavawebservice.repository.RefreshTokenRepository;
import com.example.demo.finalprojectjavawebservice.repository.UserAccountRepository;
import io.jsonwebtoken.JwtException;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserAccountRepository userAccountRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final RedisTokenBlacklistService redisTokenBlacklistService;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (AuthenticationException exception) {
            throw new UnauthorizedException("Invalid username or password");
        }

        UserAccount user = userAccountRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));
        if (user.getStatus() != AccountStatus.ACTIVE) {
            throw new UnauthorizedException("Account is locked");
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        refreshTokenRepository.save(RefreshToken.builder()
                .token(refreshToken)
                .user(user)
                .revoked(false)
                .expiresAt(jwtService.extractExpiration(refreshToken))
                .build());

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        String refreshTokenValue = request.getRefreshToken();
        try {
            jwtService.validateRefreshToken(refreshTokenValue);
        } catch (JwtException | IllegalArgumentException exception) {
            throw new UnauthorizedException("Refresh token is invalid or expired");
        }

        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .filter(token -> !token.isRevoked())
                .filter(token -> token.getExpiresAt().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> new UnauthorizedException("Refresh token is invalid or expired"));

        UserAccount user = refreshToken.getUser();
        if (user.getStatus() != AccountStatus.ACTIVE) {
            refreshToken.setRevoked(true);
            throw new UnauthorizedException("Account is locked");
        }

        String newAccessToken = jwtService.generateAccessToken(user);
        return buildAuthResponse(user, newAccessToken, refreshTokenValue);
    }

    @Transactional
    public void logout(String authorizationHeader, LogoutRequest request) {
        String accessToken = extractBearerToken(authorizationHeader);
        LocalDateTime expiresAt;
        try {
            expiresAt = jwtService.extractExpiration(accessToken);
        } catch (JwtException | IllegalArgumentException exception) {
            throw new UnauthorizedException("Access token is invalid");
        }

        redisTokenBlacklistService.blacklist(accessToken, expiresAt);

        if (request != null && StringUtils.hasText(request.getRefreshToken())) {
            refreshTokenRepository.findByToken(request.getRefreshToken())
                    .ifPresent(refreshToken -> refreshToken.setRevoked(true));
        }
    }

    @Transactional
    public MessageResponse changePassword(String username, ChangePasswordRequest request) {
        UserAccount user = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Old password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        refreshTokenRepository.revokeActiveTokensByUserId(user.getId());
        return new MessageResponse("Password changed successfully");
    }

    @Transactional
    public PasswordResetResponse forgotPassword(ForgotPasswordRequest request) {
        UserAccount user = userAccountRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Email not found"));

        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(15);
        passwordResetTokenRepository.save(PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiresAt(expiresAt)
                .used(false)
                .build());

        return new PasswordResetResponse(token, expiresAt);
    }

    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getResetToken())
                .filter(token -> !token.isUsed())
                .filter(token -> token.getExpiresAt().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> new BadRequestException("Reset token is invalid or expired"));

        UserAccount user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        resetToken.setUsed(true);
        refreshTokenRepository.revokeActiveTokensByUserId(user.getId());
        return new MessageResponse("Password reset successfully");
    }

    private AuthResponse buildAuthResponse(UserAccount user, String accessToken, String refreshToken) {
        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtService.getAccessTokenExpirationSeconds(),
                UserResponse.from(user)
        );
    }

    private String extractBearerToken(String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Bearer token is required");
        }
        return authorizationHeader.substring(7);
    }
}
