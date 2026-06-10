package com.example.demo.finalprojectjavawebservice.config;

import com.example.demo.finalprojectjavawebservice.entity.Court;
import com.example.demo.finalprojectjavawebservice.entity.TimeSlot;
import com.example.demo.finalprojectjavawebservice.entity.UserAccount;
import com.example.demo.finalprojectjavawebservice.entity.enums.AccountStatus;
import com.example.demo.finalprojectjavawebservice.entity.enums.Role;
import com.example.demo.finalprojectjavawebservice.repository.CourtRepository;
import com.example.demo.finalprojectjavawebservice.repository.TimeSlotRepository;
import com.example.demo.finalprojectjavawebservice.repository.UserAccountRepository;
import java.time.LocalTime;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserAccountRepository userAccountRepository;
    private final CourtRepository courtRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @Value("${app.seed.admin.username:admin}")
    private String adminUsername;

    @Value("${app.seed.admin.password:admin123@}")
    private String adminPassword;

    @Value("${app.seed.customer.username:customer}")
    private String customerUsername;

    @Value("${app.seed.customer.password:customer123@}")
    private String customerPassword;

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedEnabled) {
            return;
        }
        seedUsers();
        seedCourts();
        seedTimeSlots();
    }

    private void seedUsers() {
        if (!userAccountRepository.existsByUsernameIgnoreCase(adminUsername)) {
            userAccountRepository.save(UserAccount.builder()
                    .username(adminUsername)
                    .email(adminUsername + "@local.test")
                    .passwordHash(passwordEncoder.encode(adminPassword))
                    .fullName("System Admin")
                    .role(Role.ADMIN)
                    .status(AccountStatus.ACTIVE)
                    .build());
        }

        if (!userAccountRepository.existsByUsernameIgnoreCase(customerUsername)) {
            userAccountRepository.save(UserAccount.builder()
                    .username(customerUsername)
                    .email(customerUsername + "@local.test")
                    .passwordHash(passwordEncoder.encode(customerPassword))
                    .fullName("Sample Customer")
                    .role(Role.CUSTOMER)
                    .status(AccountStatus.ACTIVE)
                    .build());
        }
    }

    private void seedCourts() {
        createCourtIfMissing("Court 1", "Indoor badminton court with standard lighting");
        createCourtIfMissing("Court 2", "Indoor badminton court near reception");
        createCourtIfMissing("Court 3", "Indoor badminton court for training sessions");
    }

    private void createCourtIfMissing(String name, String description) {
        if (!courtRepository.existsByNameIgnoreCase(name)) {
            courtRepository.save(Court.builder()
                    .name(name)
                    .description(description)
                    .active(true)
                    .build());
        }
    }

    private void seedTimeSlots() {
        createTimeSlotIfMissing(LocalTime.of(7, 0), LocalTime.of(8, 0));
        createTimeSlotIfMissing(LocalTime.of(8, 0), LocalTime.of(9, 0));
        createTimeSlotIfMissing(LocalTime.of(17, 0), LocalTime.of(18, 0));
        createTimeSlotIfMissing(LocalTime.of(18, 0), LocalTime.of(19, 0));
        createTimeSlotIfMissing(LocalTime.of(19, 0), LocalTime.of(20, 0));
    }

    private void createTimeSlotIfMissing(LocalTime startTime, LocalTime endTime) {
        if (!timeSlotRepository.existsByStartTimeAndEndTime(startTime, endTime)) {
            timeSlotRepository.save(TimeSlot.builder()
                    .startTime(startTime)
                    .endTime(endTime)
                    .active(true)
                    .build());
        }
    }
}
