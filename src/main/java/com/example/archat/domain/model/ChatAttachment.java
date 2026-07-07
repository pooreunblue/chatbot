package com.example.archat.domain.model;

public record ChatAttachment(
        Long attachmentId,
        String fileName,
        String filePath,
        String mimeType,
        long fileSize,
        boolean image
) {
    public Long getAttachmentId() {
        return attachmentId;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getMimeType() {
        return mimeType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public boolean isImage() {
        return image;
    }
}
