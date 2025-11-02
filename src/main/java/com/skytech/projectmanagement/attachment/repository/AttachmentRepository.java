package com.skytech.projectmanagement.attachment.repository;

import java.util.List;
import com.skytech.projectmanagement.attachment.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepository extends JpaRepository<Attachment, Integer> {

    List<Attachment> findByEntityTypeAndEntityId(String entityType, String entityId);
}
