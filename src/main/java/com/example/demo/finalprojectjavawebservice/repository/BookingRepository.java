package com.example.demo.finalprojectjavawebservice.repository;

import com.example.demo.finalprojectjavawebservice.entity.Booking;
import com.example.demo.finalprojectjavawebservice.entity.enums.BookingStatus;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByCourtIdAndBookingDateBetweenAndStatusIn(
            Long courtId,
            LocalDateTime start,
            LocalDateTime end,
            Collection<BookingStatus> statuses
    );

    List<Booking> findByCustomerUsernameOrderByBookingDateDescCreatedAtDesc(String username);

    List<Booking> findByStatusOrderByBookingDateDescCreatedAtDesc(BookingStatus status);

    List<Booking> findAllByOrderByBookingDateDescCreatedAtDesc();
}
