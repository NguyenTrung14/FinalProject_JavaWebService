package com.example.demo.finalprojectjavawebservice.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
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
    private LocalDate bookingDate;

    @NotNull
    private Long timeSlotId;
}
