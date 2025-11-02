package com.skytech.projectmanagement.teams.mapper;


import com.skytech.projectmanagement.teams.dto.TeamMemberDTO;
import com.skytech.projectmanagement.teams.entity.TeamMember;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TeamMemberMapper {

    @Mapping(source = "teamId", target = "teamId")
    @Mapping(source = "userId", target = "userId")
    TeamMemberDTO toDto(TeamMember teamMember);

    TeamMember toEntity(TeamMemberDTO dto);
}
