package com.project.arbaz.aaspass.dto;

public record PresignedResponseDto(
        String uploadUrl,
        String key
) {
}
