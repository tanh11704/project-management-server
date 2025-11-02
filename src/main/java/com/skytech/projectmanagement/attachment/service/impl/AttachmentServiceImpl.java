package com.skytech.projectmanagement.attachment.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import com.skytech.projectmanagement.attachment.dto.AttachmentResponse;
import com.skytech.projectmanagement.attachment.entity.Attachment;
import com.skytech.projectmanagement.attachment.repository.AttachmentRepository;
import com.skytech.projectmanagement.attachment.service.AttachmentService;
import com.skytech.projectmanagement.common.exception.FileValidationException;
import com.skytech.projectmanagement.filestorage.service.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AttachmentServiceImpl implements AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional(readOnly = true)
    public List<AttachmentResponse> getAttachmentsForEntity(String entityType, String entityId) {

        List<Attachment> attachments =
                attachmentRepository.findByEntityTypeAndEntityId(entityType, entityId);

        return attachments.stream().map(attachment -> {
            String downloadUrl = null;
            try {
                downloadUrl = fileStorageService.getPresignedDownloadUrl(attachment.getFileUrl());
            } catch (Exception e) {
                log.error("Không thể tạo pre-signed URL cho file: {}", attachment.getFileUrl(), e);
                downloadUrl = "Lỗi khi lấy URL";
            }

            return AttachmentResponse.fromEntity(attachment, downloadUrl);
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AttachmentResponse createAttachment(MultipartFile file, String entityType,
            String entityId, Integer userId) {

        validateFile(file);

        String prefix = entityType.toLowerCase() + "s/" + entityId + "/";
        String objectName = fileStorageService.uploadFile(file, prefix);

        Attachment attachment = new Attachment();
        attachment.setUserId(userId);
        attachment.setEntityId(entityId);
        attachment.setEntityType(entityType);
        attachment.setFileName(
                StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename())));
        attachment.setFileUrl(objectName);

        Attachment savedAttachment = attachmentRepository.save(attachment);

        String downloadUrl =
                fileStorageService.getPresignedDownloadUrl(savedAttachment.getFileUrl());

        return AttachmentResponse.fromEntity(savedAttachment, downloadUrl);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileValidationException("File không được để trống.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !(contentType.startsWith("image/") || // (Cho phép mọi loại ảnh)
                contentType.equals("application/pdf") || // (Cho phép PDF)
                contentType.equals("application/msword") || // (Cho phép .doc)
                contentType.equals(
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))) {
            // (Cho phép .docx)

        }
    }

}
