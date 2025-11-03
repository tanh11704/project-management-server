package com.skytech.projectmanagement.user.dto;

import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Size(min = 1, message = "Tên đầy đủ không được để trống") String fullName,

        Boolean isAdmin) {

}
