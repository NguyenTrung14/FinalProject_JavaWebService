package com.example.demo.finalprojectjavawebservice.controller;

import com.example.demo.finalprojectjavawebservice.dto.response.ApiResponse;
import com.example.demo.finalprojectjavawebservice.dto.response.BookingResponse;
import com.example.demo.finalprojectjavawebservice.entity.enums.BookingStatus;
import com.example.demo.finalprojectjavawebservice.service.BookingService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/admin/bookings", "/api/v1/manager/bookings"})
@RequiredArgsConstructor
public class AdminBookingController {

    private final BookingService bookingService;

    @GetMapping
    public ApiResponse<List<BookingResponse>> getBookings(@RequestParam(required = false) BookingStatus status) {
        return ApiResponse.ok("Bookings fetched successfully", bookingService.getBookings(status));
    }

    @PutMapping("/{id}/approve")
    public ApiResponse<BookingResponse> approveBooking(@PathVariable Long id) {
        return ApiResponse.ok("Booking approved successfully", bookingService.approveBooking(id));
    }

    @PutMapping("/{id}/reject")
    public ApiResponse<BookingResponse> rejectBooking(@PathVariable Long id) {
        return ApiResponse.ok("Booking rejected successfully", bookingService.rejectBooking(id));
    }
}
