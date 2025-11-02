// File: src/main/java/com/skytech/projectmanagement/Bug/controller/BugAssigneesController.java
package com.skytech.projectmanagement.Bug.controller;

import com.skytech.projectmanagement.Bug.dto.BugAssigneesDTO;
import com.skytech.projectmanagement.Bug.service.BugAssigneesService;
import com.skytech.projectmanagement.common.dto.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/bug-service/v1/bugs/{bugId}/assignees")
@RequiredArgsConstructor
public class BugAssigneesController {

    private final BugAssigneesService bugAssigneesService;

    @PostMapping("/{userId}")
    @PreAuthorize("hasAnyAuthority('PROJECT_MANAGE_ANY', 'PROJECT_MEMBER_MANAGE')")
    public ResponseEntity<SuccessResponse<BugAssigneesDTO>> assignUserToBug(
            @PathVariable UUID bugId,
            @PathVariable Integer userId) {

        BugAssigneesDTO dto = bugAssigneesService.assignUserToBug(bugId, userId);
        return ResponseEntity.ok(SuccessResponse.of(dto, "Gán user vào bug thành công."));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAnyAuthority('PROJECT_MANAGE_ANY', 'PROJECT_MEMBER_MANAGE')")
    public ResponseEntity<SuccessResponse<Void>> unassignUserFromBug(
            @PathVariable UUID bugId,
            @PathVariable Integer userId) {

        bugAssigneesService.unassignUserFromBug(bugId, userId);
        return ResponseEntity.ok(SuccessResponse.of(null, "Bỏ gán user khỏi bug thành công."));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('PROJECT_MANAGE_ANY', 'PROJECT_MEMBER_MANAGE')")
    public ResponseEntity<SuccessResponse<List<BugAssigneesDTO>>> getAssigneesByBug(
            @PathVariable UUID bugId) {

        List<BugAssigneesDTO> list = bugAssigneesService.getAssigneesByBug(bugId);
        return ResponseEntity.ok(SuccessResponse.of(list, "Lấy danh sách assignee thành công."));
    }
}