package com.example.demo.finalprojectjavawebservice.service;

import com.example.demo.finalprojectjavawebservice.dto.request.BookingRequest;
import com.example.demo.finalprojectjavawebservice.dto.response.BookingResponse;
import com.example.demo.finalprojectjavawebservice.entity.Booking;
import com.example.demo.finalprojectjavawebservice.entity.Court;
import com.example.demo.finalprojectjavawebservice.entity.TimeSlot;
import com.example.demo.finalprojectjavawebservice.entity.UserAccount;
import com.example.demo.finalprojectjavawebservice.entity.enums.AccountStatus;
import com.example.demo.finalprojectjavawebservice.entity.enums.BookingStatus;
import com.example.demo.finalprojectjavawebservice.exception.ConflictException;
import com.example.demo.finalprojectjavawebservice.exception.ResourceNotFoundException;
import com.example.demo.finalprojectjavawebservice.repository.BookingRepository;
import com.example.demo.finalprojectjavawebservice.repository.CourtRepository;
import com.example.demo.finalprojectjavawebservice.repository.TimeSlotRepository;
import com.example.demo.finalprojectjavawebservice.repository.UserAccountRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookingService {

    private static final List<BookingStatus> BLOCKED_STATUSES = List.of(
            BookingStatus.PENDING,
            BookingStatus.CONFIRMED
    );

    private final BookingRepository bookingRepository;
    private final CourtRepository courtRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final UserAccountRepository userAccountRepository;

    @Transactional
    public BookingResponse createBooking(BookingRequest request, String username) {
        UserAccount customer = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        if (customer.getStatus() != AccountStatus.ACTIVE) {
            throw new ConflictException("Customer account is not active");
        }

        Court court = courtRepository.findById(request.getCourtId())
                .filter(Court::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Court not found or inactive"));

        TimeSlot timeSlot = timeSlotRepository.findById(request.getTimeSlotId())
                .filter(TimeSlot::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Time slot not found or inactive"));

        boolean conflicted = bookingRepository.existsByCourtIdAndBookingDateAndTimeSlotIdAndStatusIn(
                court.getId(),
                request.getBookingDate(),
                timeSlot.getId(),
                BLOCKED_STATUSES
        );
        if (conflicted) {
            throw new ConflictException("Court is already booked for this date and time slot");
        }

        Booking booking = Booking.builder()
                .customer(customer)
                .court(court)
                .timeSlot(timeSlot)
                .bookingDate(request.getBookingDate())
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
}
