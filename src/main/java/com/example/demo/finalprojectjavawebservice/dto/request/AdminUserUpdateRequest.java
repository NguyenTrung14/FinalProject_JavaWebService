package com.example.demo.finalprojectjavawebservice.dto.request;

import com.example.demo.finalprojectjavawebservice.entity.enums.AccountStatus;
import com.example.demo.finalprojectjavawebservice.entity.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserUpdateRequest {

    @Email
    @Size(max = 120)
    private String email;

    @Size(min = 6, max = 100)
    private String password;

    @Size(max = 120)
    private String fullName;

    @Size(max = 20)
    private String phoneNumber;

    private Role role;

    private AccountStatus status;
}
