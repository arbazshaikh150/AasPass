package com.project.arbaz.aaspass.service;

import com.project.arbaz.aaspass.entity.PresignedImageRecord;
import com.project.arbaz.aaspass.enums.PresignedImageStatus;
import com.project.arbaz.aaspass.repository.PresignedImageRecordRepository;
import com.project.arbaz.aaspass.security.AppOidcUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class S3Service {
    private final S3Presigner s3Presigner;
    private final PresignedImageRecordRepository presignedImageRecordRepository;
    private final S3Client s3Client;
    private static final Logger log = LoggerFactory.getLogger(S3Service.class);
    private static final Map<String, String> SUPPORTED_IMAGE_EXTENSIONS = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp"
    );
    private static final Duration UPLOAD_EXPIRATION = Duration.ofMinutes(5);
    private static final Duration S3_DELETE_RETRY_DELAY = Duration.ofMillis(250);
    private static final int S3_DELETE_BATCH_SIZE = 500;
    private static final int S3_DELETE_CONCURRENCY = 8;
    private static final int S3_DELETE_MAX_ATTEMPTS = 3;

    public record PresignedUpload(URL uploadUrl, String key) {
    }


    @Value("${aws.s3.bucket}")
    private String bucket;

    public S3Service(S3Presigner s3Presigner , PresignedImageRecordRepository presignedImageRecordRepository , S3Client s3Client) {
        this.s3Presigner = s3Presigner;
        this.presignedImageRecordRepository = presignedImageRecordRepository;
        this.s3Client = s3Client;
    }


    public PresignedUpload generateUploadUrl(String imageName, Long imageSize, String contentType, AppOidcUser currentUser) {
        validateUploadRequest(imageName, imageSize, contentType, currentUser);

        String key = getKey(imageName, contentType);
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(UPLOAD_EXPIRATION)
                .putObjectRequest(objectRequest)
                .build();

        addKeyToDb(key, currentUser.getUserId(), imageSize, imageName);

        URL uploadUrl = s3Presigner.presignPutObject(presignRequest).url();
        return new PresignedUpload(uploadUrl, key);
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
    public void addKeyToDb(String key , Long createdBy , Long size , String fileName){
        if(key == null || key.isBlank() || createdBy == null || size == null || size < 0 || fileName == null || fileName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valid image metadata is required");
        }
        PresignedImageRecord presignedImageRecord = new PresignedImageRecord();
        presignedImageRecord.setImageSize(size);
        presignedImageRecord.setCreatedBy(createdBy);
        presignedImageRecord.setFileName(fileName);
        presignedImageRecord.setPresignedImageRecordId(key);
        presignedImageRecord.setStatus(PresignedImageStatus.PENDING);
        presignedImageRecordRepository.save(presignedImageRecord);
    }

    @Transactional
    public void markPendingImagesCompleted(List<String> imageKeys, Long currentUserId) {
        if (imageKeys == null || imageKeys.isEmpty()) {
            return;
        }
        if (currentUserId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated app user is required");
        }

        Set<String> uniqueImageKeys = new HashSet<>();
        for (String imageKey : imageKeys) {
            if (imageKey == null || imageKey.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valid image keys are required");
            }
            uniqueImageKeys.add(imageKey);
        }

        List<PresignedImageRecord> imageRecords = presignedImageRecordRepository.findByIdsAndStatus(uniqueImageKeys, PresignedImageStatus.PENDING);
        if (imageRecords.size() != uniqueImageKeys.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image key is invalid, expired, or already completed");
        }
        for (PresignedImageRecord imageRecord : imageRecords) {
            if (!Objects.equals(imageRecord.getCreatedBy(), currentUserId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Image key does not belong to the current user");
            }
            imageRecord.setStatus(PresignedImageStatus.COMPLETED);
        }

        presignedImageRecordRepository.saveAll(imageRecords);
    }




    public String getKey(String imageName , String extension){
        // Random UUID generation and then using that as a key
        // Double hashing to reduce the chance of collision
        // But the size of the string is increased --> can use base64 encoding
        String key = UUID.randomUUID().toString() + "-" + imageName + UUID.randomUUID().toString();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(key.getBytes(StandardCharsets.UTF_8)) + "." + toFileExtension(extension);
    }

    private void validateUploadRequest(String imageName, Long imageSize, String contentType, AppOidcUser currentUser) {
        if (currentUser == null || currentUser.getUserId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated app user is required");
        }
        if (imageName == null || imageName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File name is required");
        }
        if (imageSize == null || imageSize <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valid image size is required");
        }
        if (contentType == null || contentType.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Content type is required");
        }
        if (!SUPPORTED_IMAGE_EXTENSIONS.containsKey(contentType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported image content type");
        }
    }

    private String toFileExtension(String contentType) {
        String extension = SUPPORTED_IMAGE_EXTENSIONS.get(contentType);
        if (extension == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported image content type");
        }
        return extension;
    }

    // Handling the delete keys from the s3
    private List<String> deleteFromS3(List<ObjectIdentifier> object){
        Delete delete = Delete.builder()
                .objects(object)
                .build();

        DeleteObjectsRequest deleteObjectsRequest = DeleteObjectsRequest.builder()
                .bucket(bucket)
                .delete(delete)
                .build();

        DeleteObjectsResponse response =  s3Client.deleteObjects(deleteObjectsRequest);
        for (S3Error error : response.errors()) {
            log.warn("Failed to delete {}: {}", error.key(), error.message());
        }
        return response.deleted()
                .stream()
                .map(DeletedObject::key)
                .toList();
    }

    private List<List<String>> partition(List<String> keys) {
        List<List<String>> batches = new ArrayList<>();

        for (int i = 0; i < keys.size(); i += S3_DELETE_BATCH_SIZE) {
            batches.add(keys.subList(i,
                    Math.min(i + S3_DELETE_BATCH_SIZE, keys.size())));
        }

        return batches;
    }

    private List<ObjectIdentifier> convert(List<String> keys) {
        return keys.stream()
                .map(key -> ObjectIdentifier.builder()
                        .key(key)
                        .build())
                .toList();
    }
    // Making a delete api call per batch and then marking all the batch as expired
    // Using Retry Mechanism as well , doing this concurrently
    private void handleExpiredImageBatchDelete(List<String> expiredPendingRecords) {
        ExecutorService executorService = Executors.newFixedThreadPool(Math.min(S3_DELETE_CONCURRENCY, expiredPendingRecords.size()));
        try {
            List<CompletableFuture<List<String>>> futures =
                    partition(expiredPendingRecords).stream()
                            .map(batch -> CompletableFuture.supplyAsync(
                                    () -> deleteFromS3WithRetry(convert(batch)),
                                    executorService))
                            .toList();
            List<String> deletedKeys = new ArrayList<>();
            for (CompletableFuture<List<String>> future : futures) {
                deletedKeys.addAll(future.join());
            }
            if (!deletedKeys.isEmpty()) {
                presignedImageRecordRepository.markPendingRecordsExpiredByIds(
                        deletedKeys,
                        PresignedImageStatus.PENDING,
                        PresignedImageStatus.EXPIRED
                );
            }
        }
        finally {
            executorService.shutdown();
        }
    }

    private List<String> deleteFromS3WithRetry(List<ObjectIdentifier> objects) {
        for (int attempt = 1; attempt <= S3_DELETE_MAX_ATTEMPTS; attempt++) {
            try {
                return deleteFromS3(objects);
            } catch (RuntimeException ex) {
                if (attempt == S3_DELETE_MAX_ATTEMPTS) {
                    return List.of();
                }
                waitBeforeRetry(attempt);
            }
        }

        return List.of();
    }

    private void waitBeforeRetry(int attempt) {
        try {
            Thread.sleep(S3_DELETE_RETRY_DELAY.toMillis() * attempt);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    @Scheduled(fixedDelay = 2000 * 60)
    @Transactional
    public void filterExpiredImages() {
        LocalDateTime expiredBefore = LocalDateTime.now().minus(UPLOAD_EXPIRATION);

        // Here cron will run and then we will delete in batches and after that marking them as Expired
        // handling batch delete
        List<String> expiredPendingRecords = presignedImageRecordRepository.findExpiredPendingRecordsForUpdate(
                expiredBefore,
                PresignedImageStatus.PENDING
        );
        if(expiredPendingRecords != null && !expiredPendingRecords.isEmpty()) {
            handleExpiredImageBatchDelete(expiredPendingRecords);
        }
    }
}
