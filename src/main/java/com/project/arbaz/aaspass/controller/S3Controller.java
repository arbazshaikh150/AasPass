package com.project.arbaz.aaspass.controller;

import com.project.arbaz.aaspass.dto.PresignedRequestDto;
import com.project.arbaz.aaspass.service.S3Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URL;

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
    public ResponseEntity<?> getPresignedUrl(@RequestBody PresignedRequestDto presignedRequestDto) {
        // Now forming the key
        if(presignedRequestDto == null || presignedRequestDto.filename() == null ||  presignedRequestDto.type() == null) {
            return ResponseEntity.badRequest().build();
        }
        URL url =  s3Service.generateUploadUrl(presignedRequestDto.filename() , presignedRequestDto.type());
        return ResponseEntity.ok(url);
    }

}
