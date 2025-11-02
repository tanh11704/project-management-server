package com.skytech.projectmanagement.user.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordsResponse {
    private List<Integer> resetUserIds;
    private List<Integer> failedUserIds;
    private int totalReset;
    private int totalFailed;
}

