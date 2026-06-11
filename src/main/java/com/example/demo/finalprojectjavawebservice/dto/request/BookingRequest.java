package com.example.demo.finalprojectjavawebservice.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {

    @NotNull
    private Long courtId;

    @NotNull
    @FutureOrPresent
    private LocalDateTime bookingDate;

    @NotNull
    @Min(30)
    private Integer durationMinutes;
}
