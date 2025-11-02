package com.skytech.projectmanagement.Bug.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssignUserRequestDTO {
    @NotNull(message = "userId là bắt buộc")
    private Integer userId;
}