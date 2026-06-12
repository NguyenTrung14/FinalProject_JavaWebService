package com.example.demo.finalprojectjavawebservice.service;

import com.example.demo.finalprojectjavawebservice.dto.response.CourtResponse;
import com.example.demo.finalprojectjavawebservice.repository.CourtRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CatalogService {

    private final CourtRepository courtRepository;

    @Transactional(readOnly = true)
    public List<CourtResponse> getActiveCourts() {
        return courtRepository.findByActiveTrueOrderByNameAsc()
                .stream()
                .map(CourtResponse::from)
                .toList();
    }
}
