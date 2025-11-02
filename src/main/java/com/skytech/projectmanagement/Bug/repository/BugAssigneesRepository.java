package com.skytech.projectmanagement.Bug.repository;

import com.skytech.projectmanagement.Bug.entity.BugAssignees;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BugAssigneesRepository extends JpaRepository<BugAssignees, Long> {

    boolean existsByBugIdAndUserId(UUID bugId, Integer userId);

    void deleteByBugIdAndUserId(UUID bugId, Integer userId);

    List<BugAssignees> findByBugId(UUID bugId);
}