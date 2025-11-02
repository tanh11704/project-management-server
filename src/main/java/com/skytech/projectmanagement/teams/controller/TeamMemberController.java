package com.skytech.projectmanagement.teams.controller;

import java.util.List;
import java.util.UUID;
import com.skytech.projectmanagement.common.dto.SuccessResponse;
import com.skytech.projectmanagement.teams.dto.TeamMemberDTO;
import com.skytech.projectmanagement.teams.entity.TeamMember;
import com.skytech.projectmanagement.teams.mapper.TeamMemberMapper;
import com.skytech.projectmanagement.teams.service.TeamMemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/teams-service/v1/members")
@RequiredArgsConstructor
public class TeamMemberController {
    private final TeamMemberService teamMemberService;
    private final TeamMemberMapper mapper;

    @GetMapping("/{teamId}")
    public ResponseEntity<?> getMembers(@PathVariable UUID teamId) {
        List<TeamMemberDTO> members = teamMemberService.getMembersByTeamId(teamId);
        return ResponseEntity
                .ok(SuccessResponse.of(members, "Lấy danh sách thành viên thành công."));
    }

    @PostMapping("/{teamId}/{userId}")
    @PreAuthorize("hasAnyAuthority('PROJECT_MANAGE_ANY', 'PROJECT_MEMBER_MANAGE')")
    public ResponseEntity<?> addMember(@PathVariable UUID teamId, @PathVariable Integer userId) {
        TeamMember member = teamMemberService.addMember(teamId, userId);

        TeamMemberDTO dto = mapper.toDto(member);

        return ResponseEntity.ok(SuccessResponse.of(dto, "Thêm thành viên vào team thành công."));
    }


    @DeleteMapping("/{teamId}/{userId}")
    @PreAuthorize("hasAnyAuthority('PROJECT_MANAGE_ANY', 'PROJECT_MEMBER_MANAGE')")
    public ResponseEntity<?> removeMember(@PathVariable UUID teamId, @PathVariable Integer userId) {
        teamMemberService.removeMember(teamId, userId);
        return ResponseEntity.ok(SuccessResponse.of(null, "Xóa thành viên khỏi team thành công."));
    }


}
