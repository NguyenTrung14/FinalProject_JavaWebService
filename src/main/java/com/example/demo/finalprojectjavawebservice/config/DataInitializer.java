package com.example.demo.finalprojectjavawebservice.config;

import com.example.demo.finalprojectjavawebservice.entity.Court;
import com.example.demo.finalprojectjavawebservice.entity.UserAccount;
import com.example.demo.finalprojectjavawebservice.entity.enums.AccountStatus;
import com.example.demo.finalprojectjavawebservice.entity.enums.Role;
import com.example.demo.finalprojectjavawebservice.repository.CourtRepository;
import com.example.demo.finalprojectjavawebservice.repository.UserAccountRepository;
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
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @Value("${app.seed.admin.username:admin}")
    private String adminUsername;

    @Value("${app.seed.admin.password:admin123@}")
    private String adminPassword;

    @Value("${app.seed.manager.username:manager}")
    private String managerUsername;

    @Value("${app.seed.manager.password:manager123@}")
    private String managerPassword;

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
    }

    private void seedUsers() {
        if (!userAccountRepository.existsByUsernameIgnoreCase(adminUsername)) {
            userAccountRepository.save(UserAccount.builder()
                    .username(adminUsername)
                    .email(adminUsername + "@gmail.com")
                    .passwordHash(passwordEncoder.encode(adminPassword))
                    .fullName("System Admin")
                    .role(Role.ADMIN)
                    .status(AccountStatus.ACTIVE)
                    .build());
        }

        if (!userAccountRepository.existsByUsernameIgnoreCase(customerUsername)) {
            userAccountRepository.save(UserAccount.builder()
                    .username(customerUsername)
                    .email(customerUsername + "@gmail.com")
                    .passwordHash(passwordEncoder.encode(customerPassword))
                    .fullName("Sample Customer")
                    .role(Role.CUSTOMER)
                    .status(AccountStatus.ACTIVE)
                    .build());
        }

        if (!userAccountRepository.existsByUsernameIgnoreCase(managerUsername)) {
            userAccountRepository.save(UserAccount.builder()
                    .username(managerUsername)
                    .email(managerUsername + "@gmail.com")
                    .passwordHash(passwordEncoder.encode(managerPassword))
                    .fullName("Sample Manager")
                    .role(Role.MANAGER)
                    .status(AccountStatus.ACTIVE)
                    .build());
        }
    }

    private void seedCourts() {
        for (int courtNumber = 1; courtNumber <= 10; courtNumber++) {
            createCourtIfMissing(
                    "Court " + courtNumber,
                    "Indoor badminton court number " + courtNumber + " with standard lighting"
            );
        }
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

}
