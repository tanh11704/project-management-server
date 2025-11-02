package com.skytech.projectmanagement.user.dto;

import java.util.List;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordsRequest {

    @NotEmpty(message = "Danh sách user ID không được để trống")
    private List<Integer> userIds;
}

