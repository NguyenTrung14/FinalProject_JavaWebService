package com.example.demo.finalprojectjavawebservice.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetResponse {

    private String resetToken;
    private LocalDateTime expiresAt;
}
