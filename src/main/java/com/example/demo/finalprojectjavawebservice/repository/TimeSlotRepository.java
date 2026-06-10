package com.example.demo.finalprojectjavawebservice.repository;

import com.example.demo.finalprojectjavawebservice.entity.TimeSlot;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    boolean existsByStartTimeAndEndTime(LocalTime startTime, LocalTime endTime);

    List<TimeSlot> findByActiveTrueOrderByStartTimeAsc();
}
