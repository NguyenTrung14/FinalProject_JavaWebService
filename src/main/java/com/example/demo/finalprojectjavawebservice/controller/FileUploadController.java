package com.example.demo.finalprojectjavawebservice.controller;

import com.example.demo.finalprojectjavawebservice.dto.response.ApiResponse;
import com.example.demo.finalprojectjavawebservice.dto.response.CourtImageResponse;
import com.example.demo.finalprojectjavawebservice.service.FileUploadService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileUploadService fileUploadService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<List<CourtImageResponse>> uploadCourtImages(
            @RequestParam Long courtId,
            @RequestPart("files") List<MultipartFile> files
    ) {
        return ApiResponse.ok("Images uploaded successfully", fileUploadService.uploadCourtImages(courtId, files));
    }
}
