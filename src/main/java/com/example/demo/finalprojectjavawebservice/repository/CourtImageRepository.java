package com.example.demo.finalprojectjavawebservice.repository;

import com.example.demo.finalprojectjavawebservice.entity.CourtImage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourtImageRepository extends JpaRepository<CourtImage, Long> {

    List<CourtImage> findByCourtIdOrderByCreatedAtDesc(Long courtId);
}
