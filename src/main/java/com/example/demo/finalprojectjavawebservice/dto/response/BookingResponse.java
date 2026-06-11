package com.example.demo.finalprojectjavawebservice.dto.response;

import com.example.demo.finalprojectjavawebservice.entity.Booking;
import com.example.demo.finalprojectjavawebservice.entity.enums.BookingStatus;
import java.time.LocalDateTime;
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
    private LocalDateTime bookingDate;
    private Integer durationMinutes;
    private LocalDateTime endTime;
    private BookingStatus status;
    private LocalDateTime createdAt;

    public static BookingResponse from(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getCustomer().getId(),
                booking.getCustomer().getUsername(),
                booking.getCourt().getId(),
                booking.getCourt().getName(),
                booking.getBookingDate(),
                booking.getDurationMinutes(),
                booking.getEndTime(),
                booking.getStatus(),
                booking.getCreatedAt()
        );
    }
}
