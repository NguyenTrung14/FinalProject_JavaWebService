package com.example.demo.finalprojectjavawebservice.repository;

import com.example.demo.finalprojectjavawebservice.entity.Court;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourtRepository extends JpaRepository<Court, Long> {

    boolean existsByNameIgnoreCase(String name);

    List<Court> findByActiveTrueOrderByNameAsc();
}
