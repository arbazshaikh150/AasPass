package com.project.arbaz.aaspass.controller;

import com.project.arbaz.aaspass.dto.PresignedRequestDto;
import com.project.arbaz.aaspass.dto.PresignedResponseDto;
import com.project.arbaz.aaspass.security.AppOidcUser;
import com.project.arbaz.aaspass.service.S3Service;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/s3")
public class S3Controller {
    private final S3Service s3Service;
    public S3Controller(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    // Now generating the presigned url
    // TODO : STORE RELEVANT INFORMATION INSIDE THE DATABASE AND OPTIMALLY QUERYING THEM
    // Only Authenticated Users can request for the presigned url
    // And there must be a limit on number of the uses per users
    @PostMapping("/presigned-url")
    public ResponseEntity<PresignedResponseDto> getPresignedUrl(
            @RequestBody PresignedRequestDto presignedRequestDto,
            @AuthenticationPrincipal AppOidcUser currentUser
    ) {
        if(presignedRequestDto == null) {
            return ResponseEntity.badRequest().build();
        }
        S3Service.PresignedUpload presignedUpload = s3Service.generateUploadUrl(
                presignedRequestDto.fileName(),
                presignedRequestDto.size(),
                presignedRequestDto.contentType(),
                currentUser
        );
        return ResponseEntity.ok(new PresignedResponseDto(
                presignedUpload.uploadUrl().toString(),
                presignedUpload.key()
        ));
    }

}
