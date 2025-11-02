package com.skytech.projectmanagement.Bug.controller;

import java.util.List;
import java.util.UUID;
import com.skytech.projectmanagement.Bug.dto.BugRequestDTO;
import com.skytech.projectmanagement.Bug.dto.BugResponseDTO;
import com.skytech.projectmanagement.Bug.service.BugService;
import com.skytech.projectmanagement.attachment.dto.AttachmentResponse;
import com.skytech.projectmanagement.attachment.service.AttachmentService;
import com.skytech.projectmanagement.common.dto.SuccessResponse;
import com.skytech.projectmanagement.user.entity.User;
import com.skytech.projectmanagement.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/bug-service/v1/bugs")
@RequiredArgsConstructor
public class BugController {
    private final BugService bugService;
    private final AttachmentService attachmentService;
    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasAuthority('BUG_CREATE')")
    public ResponseEntity<SuccessResponse<BugResponseDTO>> createBug(
            @Valid @RequestBody BugRequestDTO dto) { // ← thêm @Valid

        BugResponseDTO bug = bugService.createBug(dto);
        return ResponseEntity.ok(SuccessResponse.of(bug, "Tạo bug thành công."));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('BUG_UPDATE')")
    public ResponseEntity<SuccessResponse<BugResponseDTO>> updateBug(@PathVariable UUID id,
            @RequestBody BugRequestDTO dto) {
        BugResponseDTO updatedBug = bugService.updateBug(id, dto);
        return ResponseEntity.ok(SuccessResponse.of(updatedBug, "Cập nhật bug thành công."));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('BUG_READ_ALL')")
    public ResponseEntity<SuccessResponse<Void>> deleteBug(@PathVariable UUID id) {
        bugService.deleteBug(id);
        return ResponseEntity.ok(SuccessResponse.of(null, "Xoá bug thành công."));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('BUG_READ_ALL')")
    public ResponseEntity<SuccessResponse<BugResponseDTO>> getBugById(@PathVariable UUID id) {
        BugResponseDTO bug = bugService.getBugById(id);
        return ResponseEntity.ok(SuccessResponse.of(bug, "Lấy thông tin bug thành công."));
    }

    @GetMapping("/by-project/{projectId}")
    @PreAuthorize("hasAuthority('BUG_READ_ALL')")
    public ResponseEntity<SuccessResponse<List<BugResponseDTO>>> getBugsByProjectId(
            @PathVariable Integer projectId) {

        List<BugResponseDTO> bugs = bugService.getBugsByProjectId(projectId);
        return ResponseEntity
                .ok(SuccessResponse.of(bugs, "Lấy danh sách bug của project thành công."));
    }

    @PostMapping("/{bugId}/attachments")
    public ResponseEntity<SuccessResponse<AttachmentResponse>> uploadBugAttachment(
            @PathVariable UUID bugId, @RequestParam("file") @Valid MultipartFile file) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        User currentUser = userService.findUserByEmail(authentication.getName());
        Integer userId = currentUser.getId();

        AttachmentResponse attachmentDto =
                attachmentService.createAttachment(file, "BUG", bugId, userId);

        SuccessResponse<AttachmentResponse> response =
                SuccessResponse.of(attachmentDto, "Tải tệp lên thành công.");

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{bugId}/attachments")
    public ResponseEntity<SuccessResponse<List<AttachmentResponse>>> getBugAttachments(
            @PathVariable UUID bugId) {

        List<AttachmentResponse> attachments =
                attachmentService.getAttachmentsForEntity("BUG", bugId);

        SuccessResponse<List<AttachmentResponse>> response =
                SuccessResponse.of(attachments, "Lấy danh sách tệp đính kèm thành công.");

        return ResponseEntity.ok(response);
    }
}
