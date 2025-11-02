package com.skytech.projectmanagement.teams.mapper;

import com.skytech.projectmanagement.teams.dto.TeamsDTO;
import com.skytech.projectmanagement.teams.entity.Teams;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TeamMapper {

    TeamsDTO toDto(Teams teams);

    Teams toEntity(TeamsDTO teamsDTO);
}
