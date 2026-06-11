package com.example.demo.finalprojectjavawebservice.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.demo.finalprojectjavawebservice.dto.request.BookingRequest;
import com.example.demo.finalprojectjavawebservice.entity.Court;
import com.example.demo.finalprojectjavawebservice.entity.UserAccount;
import com.example.demo.finalprojectjavawebservice.entity.enums.AccountStatus;
import com.example.demo.finalprojectjavawebservice.entity.enums.Role;
import com.example.demo.finalprojectjavawebservice.exception.ConflictException;
import com.example.demo.finalprojectjavawebservice.repository.CourtRepository;
import com.example.demo.finalprojectjavawebservice.repository.UserAccountRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BookingServiceDuplicateValidationTests {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private CourtRepository courtRepository;

    @Test
    void createBookingRejectsOverlappingCourtSchedule() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        UserAccount customer = userAccountRepository.save(UserAccount.builder()
                .username("customer_" + suffix)
                .email("customer_" + suffix + "@example.com")
                .passwordHash("encoded-password")
                .fullName("Duplicate Booking Customer")
                .role(Role.CUSTOMER)
                .status(AccountStatus.ACTIVE)
                .build());
        Court court = courtRepository.save(Court.builder()
                .name("Duplicate Validation Court " + suffix)
                .description("Court for duplicate booking validation")
                .active(true)
                .build());
        LocalDateTime bookingDate = LocalDateTime.now()
                .plusDays(1)
                .withHour(13)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
        BookingRequest firstRequest = new BookingRequest(
                court.getId(),
                bookingDate,
                60
        );
        BookingRequest overlappingRequest = new BookingRequest(
                court.getId(),
                bookingDate.plusMinutes(30),
                30
        );

        bookingService.createBooking(firstRequest, customer.getUsername());

        assertThatThrownBy(() -> bookingService.createBooking(overlappingRequest, customer.getUsername()))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already booked");
    }
}
