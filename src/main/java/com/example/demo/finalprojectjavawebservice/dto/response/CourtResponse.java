package com.example.demo.finalprojectjavawebservice.dto.response;

import com.example.demo.finalprojectjavawebservice.entity.Court;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourtResponse {

    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private boolean active;

    public static CourtResponse from(Court court) {
        return new CourtResponse(
                court.getId(),
                court.getName(),
                court.getDescription(),
                court.getImageUrl(),
                court.isActive()
        );
    }
}
