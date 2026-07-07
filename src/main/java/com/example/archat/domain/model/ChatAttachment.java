package com.example.archat.domain.model;

public record ChatAttachment(
        String fileName,
        String filePath,
        String mimeType,
        long fileSize
) {
}
