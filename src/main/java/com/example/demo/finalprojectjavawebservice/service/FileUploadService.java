package com.example.demo.finalprojectjavawebservice.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.demo.finalprojectjavawebservice.dto.response.CourtImageResponse;
import com.example.demo.finalprojectjavawebservice.entity.Court;
import com.example.demo.finalprojectjavawebservice.entity.CourtImage;
import com.example.demo.finalprojectjavawebservice.exception.BadRequestException;
import com.example.demo.finalprojectjavawebservice.exception.CloudStorageException;
import com.example.demo.finalprojectjavawebservice.exception.ResourceNotFoundException;
import com.example.demo.finalprojectjavawebservice.repository.CourtImageRepository;
import com.example.demo.finalprojectjavawebservice.repository.CourtRepository;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FileUploadService {

    private static final long MAX_IMAGE_SIZE_BYTES = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of("image/png", "image/jpeg", "image/jpg");

    private final Cloudinary cloudinary;
    private final CourtRepository courtRepository;
    private final CourtImageRepository courtImageRepository;

    @Transactional
    public List<CourtImageResponse> uploadCourtImages(Long courtId, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new BadRequestException("At least one image file is required");
        }

        Court court = courtRepository.findById(courtId)
                .filter(Court::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Court not found or inactive"));

        return files.stream()
                .map(file -> uploadCourtImage(court, file))
                .map(CourtImageResponse::from)
                .toList();
    }

    private CourtImage uploadCourtImage(Court court, MultipartFile file) {
        validateImage(file);
        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("folder", "badminton/courts", "resource_type", "image")
            );
            String secureUrl = String.valueOf(uploadResult.get("secure_url"));
            String publicId = String.valueOf(uploadResult.get("public_id"));

            if (!StringUtils.hasText(court.getImageUrl())) {
                court.setImageUrl(secureUrl);
            }

            return courtImageRepository.save(CourtImage.builder()
                    .court(court)
                    .imageUrl(secureUrl)
                    .publicId(publicId)
                    .build());
        } catch (IOException | RuntimeException exception) {
            throw new CloudStorageException(
                    "Cloud storage service is temporarily unavailable. Please try again later.",
                    exception
            );
        }
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Image file must not be empty");
        }
        if (file.getSize() > MAX_IMAGE_SIZE_BYTES) {
            throw new BadRequestException("Image file size must not exceed 5MB");
        }
        if (!ALLOWED_IMAGE_TYPES.contains(file.getContentType())) {
            throw new BadRequestException("Only PNG and JPG images are allowed");
        }
    }
}
