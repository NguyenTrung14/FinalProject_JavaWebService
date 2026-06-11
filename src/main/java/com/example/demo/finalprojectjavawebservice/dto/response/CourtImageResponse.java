package com.example.demo.finalprojectjavawebservice.dto.response;

import com.example.demo.finalprojectjavawebservice.entity.CourtImage;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourtImageResponse {

    private Long id;
    private Long courtId;
    private String imageUrl;
    private String publicId;
    private LocalDateTime createdAt;

    public static CourtImageResponse from(CourtImage image) {
        return new CourtImageResponse(
                image.getId(),
                image.getCourt().getId(),
                image.getImageUrl(),
                image.getPublicId(),
                image.getCreatedAt()
        );
    }
}
