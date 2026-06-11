package com.example.demo.finalprojectjavawebservice.config;

import com.example.demo.finalprojectjavawebservice.dto.response.ErrorResponse;
import com.example.demo.finalprojectjavawebservice.entity.enums.AccountStatus;
import com.example.demo.finalprojectjavawebservice.repository.UserAccountRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/auth/login",
                                "/api/v1/auth/register",
                                "/api/v1/auth/refresh",
                                "/api/v1/auth/forgot-password",
                                "/api/v1/auth/reset-password"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/courts", "/api/v1/time-slots").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/files/upload").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/manager/**").hasRole("MANAGER")
                        .requestMatchers("/api/v1/customer/**").hasRole("CUSTOMER")
                        .anyRequest().authenticated())
                .httpBasic(httpBasic -> httpBasic.disable())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(this::writeUnauthorized)
                        .accessDeniedHandler(this::writeForbidden))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(UserAccountRepository userAccountRepository) {
        return username -> userAccountRepository.findByUsername(username)
                .map(user -> org.springframework.security.core.userdetails.User.builder()
                        .username(user.getUsername())
                        .password(user.getPasswordHash())
                        .authorities("ROLE_" + user.getRole().name())
                        .accountLocked(user.getStatus() != AccountStatus.ACTIVE)
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    private void writeUnauthorized(
            HttpServletRequest request,
            HttpServletResponse response,
            Exception exception
    ) throws java.io.IOException {
        writeError(response, HttpStatus.UNAUTHORIZED, "Unauthorized", "Authentication is required", request.getRequestURI());
    }

    private void writeForbidden(
            HttpServletRequest request,
            HttpServletResponse response,
            Exception exception
    ) throws java.io.IOException {
        writeError(response, HttpStatus.FORBIDDEN, "Forbidden", "Access is denied", request.getRequestURI());
    }

    private void writeError(
            HttpServletResponse response,
            HttpStatus status,
            String error,
            String message,
            String path
    ) throws java.io.IOException {
        response.setStatus(status.value());
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), ErrorResponse.of(status.value(), error, message, path));
    }
}
