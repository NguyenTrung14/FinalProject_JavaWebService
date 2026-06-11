package com.example.demo.finalprojectjavawebservice.repository;

import com.example.demo.finalprojectjavawebservice.entity.UserAccount;
import com.example.demo.finalprojectjavawebservice.entity.enums.Role;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByUsername(String username);

    Optional<UserAccount> findByEmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    @Query("""
            select u from UserAccount u
            where (:keyword is null
                   or lower(u.username) like lower(concat('%', :keyword, '%'))
                   or lower(u.email) like lower(concat('%', :keyword, '%'))
                   or lower(u.fullName) like lower(concat('%', :keyword, '%')))
              and (:role is null or u.role = :role)
            """)
    Page<UserAccount> search(@Param("keyword") String keyword, @Param("role") Role role, Pageable pageable);
}
