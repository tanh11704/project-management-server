package com.skytech.projectmanagement.Bug.controller;

import com.skytech.projectmanagement.Bug.dto.BugRequestDTO;
import com.skytech.projectmanagement.Bug.dto.BugResponseDTO;
import com.skytech.projectmanagement.Bug.service.BugService;
import com.skytech.projectmanagement.common.dto.SuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/bug-service/v1/bugs")
@RequiredArgsConstructor
public class BugController {
    private final BugService bugService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('PROJECT_MANAGE_ANY', 'PROJECT_MEMBER_MANAGE')")
    public ResponseEntity<SuccessResponse<BugResponseDTO>> createBug(
            @Valid @RequestBody BugRequestDTO dto) {  // ← thêm @Valid
        System.out.println("=== DEBUG DTO ===");
        System.out.println("projectId = " + dto.getProjectId());
        System.out.println("reporterId = " + dto.getReporterId());
        System.out.println("originalTaskId = " + dto.getOriginalTaskId());

        BugResponseDTO bug = bugService.createBug(dto);
        return ResponseEntity.ok(SuccessResponse.of(bug, "Tạo bug thành công."));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponse<BugResponseDTO>> updateBug(@PathVariable UUID id,
                                                                     @RequestBody BugRequestDTO dto) {
        BugResponseDTO updatedBug = bugService.updateBug(id, dto);
        return ResponseEntity.ok(SuccessResponse.of(updatedBug, "Cập nhật bug thành công."));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PROJECT_MANAGE_ANY', 'PROJECT_MEMBER_MANAGE')")
    public ResponseEntity<SuccessResponse<Void>> deleteBug(@PathVariable UUID id) {
        bugService.deleteBug(id);
        return ResponseEntity.ok(SuccessResponse.of(null, "Xoá bug thành công."));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PROJECT_MANAGE_ANY', 'PROJECT_MEMBER_MANAGE')")
    public ResponseEntity<SuccessResponse<BugResponseDTO>> getBugById(@PathVariable UUID id) {
        BugResponseDTO bug = bugService.getBugById(id);
        return ResponseEntity.ok(SuccessResponse.of(bug, "Lấy thông tin bug thành công."));
    }
    @GetMapping("/by-project/{projectId}")
    @PreAuthorize("hasAnyAuthority('PROJECT_MANAGE_ANY', 'PROJECT_MEMBER_MANAGE')")
    public ResponseEntity<SuccessResponse<List<BugResponseDTO>>> getBugsByProjectId(
            @PathVariable Integer projectId) {

        List<BugResponseDTO> bugs = bugService.getBugsByProjectId(projectId);
        return ResponseEntity.ok(SuccessResponse.of(bugs, "Lấy danh sách bug của project thành công."));
    }
}
