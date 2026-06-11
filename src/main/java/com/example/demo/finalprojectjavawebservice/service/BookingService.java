package com.example.demo.finalprojectjavawebservice.service;

import com.example.demo.finalprojectjavawebservice.dto.request.BookingRequest;
import com.example.demo.finalprojectjavawebservice.dto.response.BookingResponse;
import com.example.demo.finalprojectjavawebservice.entity.Booking;
import com.example.demo.finalprojectjavawebservice.entity.Court;
import com.example.demo.finalprojectjavawebservice.entity.UserAccount;
import com.example.demo.finalprojectjavawebservice.entity.enums.AccountStatus;
import com.example.demo.finalprojectjavawebservice.entity.enums.BookingStatus;
import com.example.demo.finalprojectjavawebservice.exception.BadRequestException;
import com.example.demo.finalprojectjavawebservice.exception.ConflictException;
import com.example.demo.finalprojectjavawebservice.exception.ResourceNotFoundException;
import com.example.demo.finalprojectjavawebservice.repository.BookingRepository;
import com.example.demo.finalprojectjavawebservice.repository.CourtRepository;
import com.example.demo.finalprojectjavawebservice.repository.UserAccountRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookingService {

    private static final int BOOKING_STEP_MINUTES = 30;

    private static final List<BookingStatus> BLOCKED_STATUSES = List.of(
            BookingStatus.PENDING,
            BookingStatus.CONFIRMED
    );

    private final BookingRepository bookingRepository;
    private final CourtRepository courtRepository;
    private final UserAccountRepository userAccountRepository;

    @Transactional
    public BookingResponse createBooking(BookingRequest request, String username) {
        validateBookingTime(request);

        UserAccount customer = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        if (customer.getStatus() != AccountStatus.ACTIVE) {
            throw new ConflictException("Customer account is not active");
        }

        Court court = courtRepository.findActiveByIdForUpdate(request.getCourtId())
                .orElseThrow(() -> new ResourceNotFoundException("Court not found or inactive"));

        validateScheduleAvailable(court.getId(), request.getBookingDate(), request.getDurationMinutes(), null);

        Booking booking = Booking.builder()
                .customer(customer)
                .court(court)
                .bookingDate(request.getBookingDate())
                .durationMinutes(request.getDurationMinutes())
                .status(BookingStatus.PENDING)
                .build();

        return BookingResponse.from(bookingRepository.save(booking));
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getMyBookings(String username) {
        return bookingRepository.findByCustomerUsernameOrderByBookingDateDescCreatedAtDesc(username)
                .stream()
                .map(BookingResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getBookings(BookingStatus status) {
        List<Booking> bookings = status == null
                ? bookingRepository.findAllByOrderByBookingDateDescCreatedAtDesc()
                : bookingRepository.findByStatusOrderByBookingDateDescCreatedAtDesc(status);
        return bookings.stream()
                .map(BookingResponse::from)
                .toList();
    }

    @Transactional
    public BookingResponse approveBooking(Long id) {
        Booking booking = findBooking(id);
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new ConflictException("Only pending bookings can be approved");
        }

        courtRepository.findActiveByIdForUpdate(booking.getCourt().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Court not found or inactive"));
        validateScheduleAvailable(
                booking.getCourt().getId(),
                booking.getBookingDate(),
                booking.getDurationMinutes(),
                booking.getId()
        );

        booking.setStatus(BookingStatus.CONFIRMED);
        return BookingResponse.from(booking);
    }

    @Transactional
    public BookingResponse rejectBooking(Long id) {
        Booking booking = findBooking(id);
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new ConflictException("Only pending bookings can be rejected");
        }

        booking.setStatus(BookingStatus.REJECTED);
        return BookingResponse.from(booking);
    }

    private Booking findBooking(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
    }

    private void validateBookingTime(BookingRequest request) {
        LocalDateTime bookingDate = request.getBookingDate();
        if (bookingDate == null || request.getDurationMinutes() == null) {
            throw new BadRequestException("Booking date and duration are required");
        }
        if (bookingDate.getMinute() != 0 && bookingDate.getMinute() != 30) {
            throw new BadRequestException("Booking time must start at hour or half-hour");
        }
        if (request.getDurationMinutes() < BOOKING_STEP_MINUTES) {
            throw new BadRequestException("Booking duration must be at least 30 minutes");
        }
        if (bookingDate.getSecond() != 0 || bookingDate.getNano() != 0) {
            throw new BadRequestException("Booking time must not include seconds or nanoseconds");
        }
        if (request.getDurationMinutes() % BOOKING_STEP_MINUTES != 0) {
            throw new BadRequestException("Booking duration must be divisible by 30 minutes");
        }
    }

    private void validateScheduleAvailable(
            Long courtId,
            LocalDateTime bookingDate,
            Integer durationMinutes,
            Long ignoredBookingId
    ) {
        LocalDateTime requestedEndTime = bookingDate.plusMinutes(durationMinutes);
        LocalDateTime dayStart = bookingDate.toLocalDate().atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);

        boolean conflicted = bookingRepository.findByCourtIdAndBookingDateBetweenAndStatusIn(
                courtId,
                dayStart,
                dayEnd,
                BLOCKED_STATUSES
        ).stream()
                .filter(booking -> ignoredBookingId == null || !booking.getId().equals(ignoredBookingId))
                .anyMatch(booking -> bookingDate.isBefore(booking.getEndTime())
                        && requestedEndTime.isAfter(booking.getBookingDate()));

        if (conflicted) {
            throw new ConflictException("Court is already booked for this date and time slot");
        }
    }
}
