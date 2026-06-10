package com.example.demo.finalprojectjavawebservice.dto.response;

import com.example.demo.finalprojectjavawebservice.entity.Booking;
import com.example.demo.finalprojectjavawebservice.entity.enums.BookingStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {

    private Long id;
    private Long customerId;
    private String customerUsername;
    private Long courtId;
    private String courtName;
    private Long timeSlotId;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDate bookingDate;
    private BookingStatus status;
    private LocalDateTime createdAt;

    public static BookingResponse from(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getCustomer().getId(),
                booking.getCustomer().getUsername(),
                booking.getCourt().getId(),
                booking.getCourt().getName(),
                booking.getTimeSlot().getId(),
                booking.getTimeSlot().getStartTime(),
                booking.getTimeSlot().getEndTime(),
                booking.getBookingDate(),
                booking.getStatus(),
                booking.getCreatedAt()
        );
    }
}
