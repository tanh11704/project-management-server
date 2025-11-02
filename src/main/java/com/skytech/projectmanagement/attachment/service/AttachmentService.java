package com.skytech.projectmanagement.attachment.service;

import java.util.List;
import java.util.UUID;
import com.skytech.projectmanagement.attachment.dto.AttachmentResponse;
import org.springframework.web.multipart.MultipartFile;

public interface AttachmentService {

    List<AttachmentResponse> getAttachmentsForEntity(String entityType, String entityId);

    AttachmentResponse createAttachment(MultipartFile file, String entityType, String entityId,
            Integer userId);

    // Overloaded methods for Integer entityId (backward compatibility)
    default List<AttachmentResponse> getAttachmentsForEntity(String entityType, Integer entityId) {
        return getAttachmentsForEntity(entityType, String.valueOf(entityId));
    }

    default AttachmentResponse createAttachment(MultipartFile file, String entityType,
            Integer entityId, Integer userId) {
        return createAttachment(file, entityType, String.valueOf(entityId), userId);
    }

    // Overloaded methods for UUID entityId
    default List<AttachmentResponse> getAttachmentsForEntity(String entityType, UUID entityId) {
        return getAttachmentsForEntity(entityType, entityId.toString());
    }

    default AttachmentResponse createAttachment(MultipartFile file, String entityType,
            UUID entityId, Integer userId) {
        return createAttachment(file, entityType, entityId.toString(), userId);
    }
}
