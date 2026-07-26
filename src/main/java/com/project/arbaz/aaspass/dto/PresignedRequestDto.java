package com.project.arbaz.aaspass.dto;

public record PresignedRequestDto(
        String filename,
        Long size,
        String type
) {
}
