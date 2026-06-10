package com.example.demo.finalprojectjavawebservice.repository;

import com.example.demo.finalprojectjavawebservice.entity.TokenBlacklist;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {

    boolean existsByToken(String token);

    boolean existsByTokenAndExpiresAtAfter(String token, LocalDateTime now);
}
