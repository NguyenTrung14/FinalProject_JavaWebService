package com.example.demo.finalprojectjavawebservice.service;

import com.example.demo.finalprojectjavawebservice.dto.request.AdminUserCreateRequest;
import com.example.demo.finalprojectjavawebservice.dto.request.AdminUserUpdateRequest;
import com.example.demo.finalprojectjavawebservice.dto.request.RegisterRequest;
import com.example.demo.finalprojectjavawebservice.dto.response.UserResponse;
import com.example.demo.finalprojectjavawebservice.entity.UserAccount;
import com.example.demo.finalprojectjavawebservice.entity.enums.AccountStatus;
import com.example.demo.finalprojectjavawebservice.entity.enums.Role;
import com.example.demo.finalprojectjavawebservice.exception.ConflictException;
import com.example.demo.finalprojectjavawebservice.exception.ResourceNotFoundException;
import com.example.demo.finalprojectjavawebservice.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        validateUniqueUsernameAndEmail(request.getUsername(), request.getEmail());
        UserAccount user = UserAccount.builder()
                .username(request.getUsername().trim())
                .email(request.getEmail().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName().trim())
                .phoneNumber(normalize(request.getPhoneNumber()))
                .role(Role.CUSTOMER)
                .status(AccountStatus.ACTIVE)
                .build();
        return UserResponse.from(userAccountRepository.save(user));
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> searchUsers(String keyword, Role role, Pageable pageable) {
        String normalizedKeyword = StringUtils.hasText(keyword) ? keyword.trim() : null;
        return userAccountRepository.search(normalizedKeyword, role, pageable)
                .map(UserResponse::from);
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(Long id) {
        return UserResponse.from(findUser(id));
    }

    @Transactional
    public UserResponse createUser(AdminUserCreateRequest request) {
        validateUniqueUsernameAndEmail(request.getUsername(), request.getEmail());
        UserAccount user = UserAccount.builder()
                .username(request.getUsername().trim())
                .email(request.getEmail().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName().trim())
                .phoneNumber(normalize(request.getPhoneNumber()))
                .role(request.getRole())
                .status(request.getStatus() == null ? AccountStatus.ACTIVE : request.getStatus())
                .build();
        return UserResponse.from(userAccountRepository.save(user));
    }

    @Transactional
    public UserResponse updateUser(Long id, AdminUserUpdateRequest request) {
        UserAccount user = findUser(id);

        if (StringUtils.hasText(request.getEmail()) && !request.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (userAccountRepository.existsByEmailIgnoreCase(request.getEmail())) {
                throw new ConflictException("Email already exists");
            }
            user.setEmail(request.getEmail().trim());
        }
        if (StringUtils.hasText(request.getPassword())) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        if (StringUtils.hasText(request.getFullName())) {
            user.setFullName(request.getFullName().trim());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(normalize(request.getPhoneNumber()));
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }

        return UserResponse.from(userAccountRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        UserAccount user = findUser(id);
        userAccountRepository.delete(user);
    }

    private UserAccount findUser(Long id) {
        return userAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void validateUniqueUsernameAndEmail(String username, String email) {
        if (userAccountRepository.existsByUsernameIgnoreCase(username)) {
            throw new ConflictException("Username already exists");
        }
        if (userAccountRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Email already exists");
        }
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
