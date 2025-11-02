package com.skytech.projectmanagement.auth.dto;

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
public class SyncUserPermissionsRequest {

    @NotEmpty(message = "Danh sách permission ID không được để trống")
    private List<Integer> permissionIds;
}

