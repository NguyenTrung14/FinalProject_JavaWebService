package com.example.demo.finalprojectjavawebservice.controller;

import com.example.demo.finalprojectjavawebservice.dto.request.BookingRequest;
import com.example.demo.finalprojectjavawebservice.dto.response.ApiResponse;
import com.example.demo.finalprojectjavawebservice.dto.response.BookingResponse;
import com.example.demo.finalprojectjavawebservice.service.BookingService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customer/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @Valid @RequestBody BookingRequest request,
            Principal principal
    ) {
        BookingResponse response = bookingService.createBooking(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Booking created successfully", response));
    }

    @GetMapping({"", "/history"})
    public ApiResponse<List<BookingResponse>> getMyBookings(Principal principal) {
        return ApiResponse.ok("Bookings fetched successfully", bookingService.getMyBookings(principal.getName()));
    }
}
