package com.project.arbaz.aaspass.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public record PresignedRequestDto(
        @JsonAlias("filename")
        String fileName,
        Long size,
        @JsonAlias("type")
        String contentType
) {
}
