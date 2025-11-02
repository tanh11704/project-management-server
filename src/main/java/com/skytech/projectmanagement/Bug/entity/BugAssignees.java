package com.skytech.projectmanagement.Bug.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "bug_assignees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BugAssignees {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bug_id", nullable = false)
    private UUID bugId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;
}