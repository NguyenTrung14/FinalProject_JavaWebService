package com.example.demo.finalprojectjavawebservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.finalprojectjavawebservice.dto.request.BookingRequest;
import com.example.demo.finalprojectjavawebservice.dto.request.ChangePasswordRequest;
import com.example.demo.finalprojectjavawebservice.dto.request.ForgotPasswordRequest;
import com.example.demo.finalprojectjavawebservice.dto.response.PasswordResetResponse;
import com.example.demo.finalprojectjavawebservice.entity.Booking;
import com.example.demo.finalprojectjavawebservice.entity.Court;
import com.example.demo.finalprojectjavawebservice.entity.PasswordResetToken;
import com.example.demo.finalprojectjavawebservice.entity.UserAccount;
import com.example.demo.finalprojectjavawebservice.entity.enums.AccountStatus;
import com.example.demo.finalprojectjavawebservice.entity.enums.BookingStatus;
import com.example.demo.finalprojectjavawebservice.entity.enums.Role;
import com.example.demo.finalprojectjavawebservice.exception.BadRequestException;
import com.example.demo.finalprojectjavawebservice.exception.ConflictException;
import com.example.demo.finalprojectjavawebservice.exception.UnauthorizedException;
import com.example.demo.finalprojectjavawebservice.repository.BookingRepository;
import com.example.demo.finalprojectjavawebservice.repository.CourtRepository;
import com.example.demo.finalprojectjavawebservice.repository.PasswordResetTokenRepository;
import com.example.demo.finalprojectjavawebservice.repository.RefreshTokenRepository;
import com.example.demo.finalprojectjavawebservice.repository.UserAccountRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

class ServiceUnitTests {

    @Test
    void bookingServiceRejectsStartMinuteThatIsNotHourOrHalfHour() {
        BookingService bookingService = new BookingService(
                mock(BookingRepository.class),
                mock(CourtRepository.class),
                mock(UserAccountRepository.class)
        );
        BookingRequest request = new BookingRequest(1L, LocalDateTime.of(2026, 6, 12, 9, 15), 30);

        assertThatThrownBy(() -> bookingService.createBooking(request, "customer"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("hour or half-hour");
    }

    @Test
    void bookingServiceRejectsOverlappingSchedule() {
        BookingRepository bookingRepository = mock(BookingRepository.class);
        CourtRepository courtRepository = mock(CourtRepository.class);
        UserAccountRepository userAccountRepository = mock(UserAccountRepository.class);
        BookingService bookingService = new BookingService(bookingRepository, courtRepository, userAccountRepository);

        UserAccount customer = UserAccount.builder()
                .id(1L)
                .username("customer")
                .status(AccountStatus.ACTIVE)
                .role(Role.CUSTOMER)
                .build();
        Court court = Court.builder()
                .id(1L)
                .name("Court 1")
                .active(true)
                .build();
        Booking existingBooking = Booking.builder()
                .id(10L)
                .customer(customer)
                .court(court)
                .bookingDate(LocalDateTime.of(2026, 6, 12, 9, 30))
                .durationMinutes(60)
                .status(BookingStatus.PENDING)
                .build();

        when(userAccountRepository.findByUsername("customer")).thenReturn(Optional.of(customer));
        when(courtRepository.findActiveByIdForUpdate(1L)).thenReturn(Optional.of(court));
        when(bookingRepository.findByCourtIdAndBookingDateBetweenAndStatusIn(eq(1L), any(), any(), any()))
                .thenReturn(List.of(existingBooking));

        BookingRequest request = new BookingRequest(1L, LocalDateTime.of(2026, 6, 12, 9, 0), 60);

        assertThatThrownBy(() -> bookingService.createBooking(request, "customer"))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already booked");
    }

    @Test
    void authServiceChangePasswordRejectsWrongOldPassword() {
        UserAccountRepository userAccountRepository = mock(UserAccountRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        AuthService authService = new AuthService(
                mock(AuthenticationManager.class),
                mock(JwtService.class),
                passwordEncoder,
                userAccountRepository,
                mock(RefreshTokenRepository.class),
                mock(PasswordResetTokenRepository.class),
                mock(RedisTokenBlacklistService.class)
        );
        UserAccount user = UserAccount.builder()
                .id(1L)
                .username("customer")
                .passwordHash("hash")
                .build();

        when(userAccountRepository.findByUsername("customer")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.changePassword(
                "customer",
                new ChangePasswordRequest("wrong-password", "newPassword123")
        )).isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void authServiceForgotPasswordCreatesResetToken() {
        UserAccountRepository userAccountRepository = mock(UserAccountRepository.class);
        PasswordResetTokenRepository resetTokenRepository = mock(PasswordResetTokenRepository.class);
        AuthService authService = new AuthService(
                mock(AuthenticationManager.class),
                mock(JwtService.class),
                mock(PasswordEncoder.class),
                userAccountRepository,
                mock(RefreshTokenRepository.class),
                resetTokenRepository,
                mock(RedisTokenBlacklistService.class)
        );
        UserAccount user = UserAccount.builder()
                .id(1L)
                .email("customer@example.com")
                .build();

        when(userAccountRepository.findByEmailIgnoreCase("customer@example.com")).thenReturn(Optional.of(user));
        when(resetTokenRepository.save(any(PasswordResetToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PasswordResetResponse response = authService.forgotPassword(new ForgotPasswordRequest("customer@example.com"));

        assertThat(response.getResetToken()).isNotBlank();
        assertThat(response.getExpiresAt()).isAfter(LocalDateTime.now());
    }

    @Test
    void redisTokenBlacklistServiceStoresTokenWithTtl() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        RedisTokenBlacklistService service = new RedisTokenBlacklistService(redisTemplate);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.hasKey("auth:blacklist:access-token")).thenReturn(true);

        service.blacklist("access-token", LocalDateTime.now().plusMinutes(5));

        verify(valueOperations).set(eq("auth:blacklist:access-token"), eq("revoked"), any(Duration.class));
        assertThat(service.isBlacklisted("access-token")).isTrue();
    }
}
