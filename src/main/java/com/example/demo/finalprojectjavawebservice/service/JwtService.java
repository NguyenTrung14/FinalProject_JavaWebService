package com.example.demo.finalprojectjavawebservice.service;

import com.example.demo.finalprojectjavawebservice.entity.UserAccount;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final String TOKEN_TYPE_CLAIM = "type";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.access-token-expiration-minutes}")
    private long accessTokenExpirationMinutes;

    @Value("${app.jwt.refresh-token-expiration-days}")
    private long refreshTokenExpirationDays;

    public String generateAccessToken(UserAccount user) {
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(accessTokenExpirationMinutes);
        return generateToken(user, ACCESS_TOKEN_TYPE, expiresAt);
    }

    public String generateRefreshToken(UserAccount user) {
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(refreshTokenExpirationDays);
        return generateToken(user, REFRESH_TOKEN_TYPE, expiresAt);
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public LocalDateTime extractExpiration(String token) {
        return toLocalDateTime(extractAllClaims(token).getExpiration());
    }

    public long getAccessTokenExpirationSeconds() {
        return accessTokenExpirationMinutes * 60;
    }

    public boolean isAccessTokenValid(String token, UserDetails userDetails) {
        Claims claims = extractAllClaims(token);
        return ACCESS_TOKEN_TYPE.equals(claims.get(TOKEN_TYPE_CLAIM, String.class))
                && userDetails.getUsername().equals(claims.getSubject())
                && claims.getExpiration().after(new Date());
    }

    public void validateRefreshToken(String token) {
        Claims claims = extractAllClaims(token);
        if (!REFRESH_TOKEN_TYPE.equals(claims.get(TOKEN_TYPE_CLAIM, String.class))) {
            throw new JwtException("Token is not a refresh token");
        }
        if (!claims.getExpiration().after(new Date())) {
            throw new JwtException("Refresh token has expired");
        }
    }

    private String generateToken(UserAccount user, String tokenType, LocalDateTime expiresAt) {
        return Jwts.builder()
                .subject(user.getUsername())
                .claim(TOKEN_TYPE_CLAIM, tokenType)
                .claim("role", user.getRole().name())
                .issuedAt(new Date())
                .expiration(toDate(expiresAt))
                .signWith(getSigningKey())
                .compact();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    private Date toDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    private LocalDateTime toLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }
}
