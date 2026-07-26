package com.project.arbaz.aaspass.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URL;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;

@Service
public class S3Service {
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket}")
    private String bucket;

    public S3Service(S3Presigner s3Presigner) {
        this.s3Presigner = s3Presigner;
    }


    // Should contain the metadata related to the images and should be stored in the database
    // Then there must be a cleanup mechanism , which will make the application consistent.
    public URL generateUploadUrl(String imageName, String contentType) {
        String key = getKey(imageName, contentType);
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5))
                .putObjectRequest(objectRequest)
                .build();

        return s3Presigner.presignPutObject(presignRequest).url();
    }

//    public URL generateViewUrl(String imageName, String contentType) {
//        String key = getKey(imageName, contentType);
//        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
//                .bucket(bucket)
//                .key(key)
//                .build();
//        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
//                .signatureDuration(Duration.ofMinutes(5))
//                .getObjectRequest(getObjectRequest)
//                .build();
//        return s3Presigner.presignGetObject(presignRequest).url();
//    }

    public String getKey(String imageName , String extension){
        // Random UUID generation and then using that as a key
        // Double hashing to reduce the chance of collision
        // But the size of the string is increased --> can use base64 encoding
        String key = UUID.randomUUID().toString() + "-" + imageName + UUID.randomUUID().toString();
        return Base64.getEncoder().encodeToString(key.getBytes()) + "." + extension;
    }
}
