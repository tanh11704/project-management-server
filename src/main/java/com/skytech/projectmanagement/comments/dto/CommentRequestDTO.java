package com.skytech.projectmanagement.comments.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.skytech.projectmanagement.comments.entity.EntityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequestDTO {
        @NotBlank(message = "Nội dung không được để trống")
        private String body;

        @NotNull(message = "entityId không được null")
        @JsonProperty("entityId")
        private String entityId;

        @NotNull(message = "entityType không được null")
        @JsonProperty("entityType")
        private EntityType entityType;
}