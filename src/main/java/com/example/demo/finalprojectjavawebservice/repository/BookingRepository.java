package com.example.demo.finalprojectjavawebservice.repository;

import com.example.demo.finalprojectjavawebservice.entity.Booking;
import com.example.demo.finalprojectjavawebservice.entity.enums.BookingStatus;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    boolean existsByCourtIdAndBookingDateAndTimeSlotIdAndStatusIn(
            Long courtId,
            LocalDate bookingDate,
            Long timeSlotId,
            Collection<BookingStatus> statuses
    );

    List<Booking> findByCustomerUsernameOrderByBookingDateDescCreatedAtDesc(String username);
}
