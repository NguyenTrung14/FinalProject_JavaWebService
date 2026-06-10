package com.example.demo.finalprojectjavawebservice.dto.response;

import com.example.demo.finalprojectjavawebservice.entity.TimeSlot;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlotResponse {

    private Long id;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean active;

    public static TimeSlotResponse from(TimeSlot timeSlot) {
        return new TimeSlotResponse(
                timeSlot.getId(),
                timeSlot.getStartTime(),
                timeSlot.getEndTime(),
                timeSlot.isActive()
        );
    }
}
