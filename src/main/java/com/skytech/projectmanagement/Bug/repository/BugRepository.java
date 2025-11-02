package com.skytech.projectmanagement.Bug.repository;

import com.skytech.projectmanagement.Bug.entity.Bug;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
@Repository
public interface BugRepository  extends JpaRepository<Bug, UUID> {
    List<Bug> findByProjectId(Integer projectId);
}
