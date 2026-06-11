package com.example.demo.finalprojectjavawebservice.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.finalprojectjavawebservice.dto.request.BookingRequest;
import com.example.demo.finalprojectjavawebservice.dto.request.ChangePasswordRequest;
import com.example.demo.finalprojectjavawebservice.dto.request.LoginRequest;
import com.example.demo.finalprojectjavawebservice.dto.response.AuthResponse;
import com.example.demo.finalprojectjavawebservice.dto.response.BookingResponse;
import com.example.demo.finalprojectjavawebservice.dto.response.MessageResponse;
import com.example.demo.finalprojectjavawebservice.dto.response.UserResponse;
import com.example.demo.finalprojectjavawebservice.entity.enums.AccountStatus;
import com.example.demo.finalprojectjavawebservice.entity.enums.BookingStatus;
import com.example.demo.finalprojectjavawebservice.entity.enums.Role;
import com.example.demo.finalprojectjavawebservice.service.AuthService;
import com.example.demo.finalprojectjavawebservice.service.BookingService;
import com.example.demo.finalprojectjavawebservice.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ControllerUnitTests {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Test
    void authControllerLoginReturnsTokenResponse() throws Exception {
        AuthService authService = mock(AuthService.class);
        AuthController controller = new AuthController(mock(UserService.class), authService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        UserResponse user = new UserResponse(
                1L,
                "customer",
                "customer@example.com",
                "Customer",
                null,
                Role.CUSTOMER,
                AccountStatus.ACTIVE,
                null,
                null
        );

        when(authService.login(any(LoginRequest.class)))
                .thenReturn(new AuthResponse("access-token", "refresh-token", "Bearer", 1800, user));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("customer", "customer123@"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"));
    }

    @Test
    void authControllerChangePasswordReturnsMessage() throws Exception {
        AuthService authService = mock(AuthService.class);
        AuthController controller = new AuthController(mock(UserService.class), authService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        when(authService.changePassword(eq("customer"), any(ChangePasswordRequest.class)))
                .thenReturn(new MessageResponse("Password changed successfully"));

        mockMvc.perform(post("/api/v1/auth/change-password")
                        .principal(() -> "customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ChangePasswordRequest("oldPassword123", "newPassword123")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("Password changed successfully"));
    }

    @Test
    void bookingControllerCreateBookingReturnsCreated() throws Exception {
        BookingService bookingService = mock(BookingService.class);
        BookingController controller = new BookingController(bookingService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        LocalDateTime bookingDate = LocalDateTime.of(2026, 6, 12, 9, 30);
        BookingRequest request = new BookingRequest(1L, bookingDate, 60);

        when(bookingService.createBooking(any(BookingRequest.class), eq("customer")))
                .thenReturn(bookingResponse(1L, BookingStatus.PENDING));

        mockMvc.perform(post("/api/v1/customer/bookings")
                        .principal(() -> "customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void bookingControllerHistoryReturnsCustomerBookings() throws Exception {
        BookingService bookingService = mock(BookingService.class);
        BookingController controller = new BookingController(bookingService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        when(bookingService.getMyBookings("customer")).thenReturn(List.of(bookingResponse(1L, BookingStatus.PENDING)));

        mockMvc.perform(get("/api/v1/customer/bookings/history").principal(() -> "customer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id").value(1L));
    }

    @Test
    void adminBookingControllerApproveBookingReturnsConfirmedBooking() throws Exception {
        BookingService bookingService = mock(BookingService.class);
        AdminBookingController controller = new AdminBookingController(bookingService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        when(bookingService.approveBooking(1L)).thenReturn(bookingResponse(1L, BookingStatus.CONFIRMED));

        mockMvc.perform(put("/api/v1/admin/bookings/1/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"));
    }

    private BookingResponse bookingResponse(Long id, BookingStatus status) {
        LocalDateTime bookingDate = LocalDateTime.of(2026, 6, 12, 9, 30);
        return new BookingResponse(
                id,
                1L,
                "customer",
                1L,
                "Court 1",
                bookingDate,
                60,
                bookingDate.plusMinutes(60),
                status,
                LocalDateTime.of(2026, 6, 11, 12, 0)
        );
    }
}
