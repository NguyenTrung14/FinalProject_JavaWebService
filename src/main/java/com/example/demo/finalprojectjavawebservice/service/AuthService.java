package com.example.demo.finalprojectjavawebservice.service;

import com.example.demo.finalprojectjavawebservice.dto.request.LoginRequest;
import com.example.demo.finalprojectjavawebservice.dto.request.LogoutRequest;
import com.example.demo.finalprojectjavawebservice.dto.request.RefreshTokenRequest;
import com.example.demo.finalprojectjavawebservice.dto.response.AuthResponse;
import com.example.demo.finalprojectjavawebservice.dto.response.UserResponse;
import com.example.demo.finalprojectjavawebservice.entity.RefreshToken;
import com.example.demo.finalprojectjavawebservice.entity.TokenBlacklist;
import com.example.demo.finalprojectjavawebservice.entity.UserAccount;
import com.example.demo.finalprojectjavawebservice.entity.enums.AccountStatus;
import com.example.demo.finalprojectjavawebservice.exception.UnauthorizedException;
import com.example.demo.finalprojectjavawebservice.repository.RefreshTokenRepository;
import com.example.demo.finalprojectjavawebservice.repository.TokenBlacklistRepository;
import com.example.demo.finalprojectjavawebservice.repository.UserAccountRepository;
import io.jsonwebtoken.JwtException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserAccountRepository userAccountRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenBlacklistRepository tokenBlacklistRepository;

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

        if (!tokenBlacklistRepository.existsByToken(accessToken)) {
            tokenBlacklistRepository.save(TokenBlacklist.builder()
                    .token(accessToken)
                    .expiresAt(expiresAt)
                    .build());
        }

        if (request != null && StringUtils.hasText(request.getRefreshToken())) {
            refreshTokenRepository.findByToken(request.getRefreshToken())
                    .ifPresent(refreshToken -> refreshToken.setRevoked(true));
        }
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
