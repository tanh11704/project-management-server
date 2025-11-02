package com.skytech.projectmanagement.teams.service.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import com.skytech.projectmanagement.common.exception.DeleteConflictException;
import com.skytech.projectmanagement.common.exception.ResourceNotFoundException;
import com.skytech.projectmanagement.teams.dto.TeamMemberDTO;
import com.skytech.projectmanagement.teams.dto.TeamsDTO;
import com.skytech.projectmanagement.teams.entity.TeamMember;
import com.skytech.projectmanagement.teams.mapper.TeamMemberMapper;
import com.skytech.projectmanagement.teams.repository.TeamMemberRepository;
import com.skytech.projectmanagement.teams.service.TeamMemberService;
import com.skytech.projectmanagement.teams.service.TeamService;
import com.skytech.projectmanagement.user.dto.UserResponse;
import com.skytech.projectmanagement.user.service.UserService;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TeamMemberServiceImpl implements TeamMemberService {

    private final TeamMemberRepository teamMemberRepository;
    private final TeamService teamService;
    private final UserService userService;
    private final TeamMemberMapper mapper;

    @Override
    public List<TeamMemberDTO> getMembersByTeamId(UUID teamId) {
        teamService.getTeamById(teamId);
        List<TeamMember> members = teamMemberRepository.findByTeamId(teamId);

        return members.stream().map(member -> {
            TeamsDTO team = teamService.getTeamById(member.getTeamId());
            UserResponse user = userService.getUserById(member.getUserId());

            TeamMemberDTO dto = mapper.toDto(member);

            dto.setTeamName(team.getGroupName());
            dto.setFullName(user.fullName());
            dto.setEmail(user.email());
            dto.setAvatar(user.avatarUrl());
            // dto.setIsProductOwner(user.isProductOwner());

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public TeamMember addMember(UUID teamId, Integer userId) {
        // Kiểm tra tồn tại
        teamService.getTeamById(teamId);
        userService.getUserById(userId);

        boolean exists = teamMemberRepository.findByTeamIdAndUserId(teamId, userId).isPresent();
        if (exists) {
            throw new DeleteConflictException("Người dùng đã là thành viên của team này");
        }

        // Tạo mới thành viên
        TeamMember member = new TeamMember();
        member.setTeamId(teamId);
        member.setUserId(userId);
        return teamMemberRepository.save(member);
    }

    @Override
    public void removeMember(UUID teamId, Integer userId) {
        TeamMember member = teamMemberRepository.findByTeamIdAndUserId(teamId, userId).orElseThrow(
                () -> new ResourceNotFoundException("Người dùng không phải thành viên của team"));
        teamMemberRepository.delete(member);
    }
}
