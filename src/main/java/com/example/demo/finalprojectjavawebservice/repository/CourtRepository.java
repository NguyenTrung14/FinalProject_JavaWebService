package com.example.demo.finalprojectjavawebservice.repository;

import com.example.demo.finalprojectjavawebservice.entity.Court;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourtRepository extends JpaRepository<Court, Long> {

    boolean existsByNameIgnoreCase(String name);

    List<Court> findByActiveTrueOrderByNameAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Court c where c.id = :id and c.active = true")
    Optional<Court> findActiveByIdForUpdate(@Param("id") Long id);
}
