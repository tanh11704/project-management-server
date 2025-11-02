// File: src/main/java/com/skytech/projectmanagement/Bug/service/BugAssigneesServiceImpl.java
package com.skytech.projectmanagement.Bug.service;

import com.skytech.projectmanagement.Bug.Mapper.BugAssigneesMapper;
import com.skytech.projectmanagement.Bug.dto.BugAssigneesDTO;
import com.skytech.projectmanagement.Bug.entity.BugAssignees;
import com.skytech.projectmanagement.Bug.repository.BugAssigneesRepository;
import com.skytech.projectmanagement.common.exception.DeleteConflictException;
import com.skytech.projectmanagement.common.exception.ResourceNotFoundException;
import com.skytech.projectmanagement.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class BugAssigneesServiceImpl implements BugAssigneesService {

    private final BugAssigneesRepository bugAssigneesRepository;
    private final UserService userService;
    private final BugService bugService;  // Đảm bảo có method getBugById(UUID)
    private final BugAssigneesMapper bugAssigneesMapper;

    @Override
    public BugAssigneesDTO assignUserToBug(UUID bugId, Integer userId) {
        // Kiểm tra tồn tại
        userService.getUserById(userId);
        bugService.getBugById(bugId);  // Throw nếu không tồn tại

        // Kiểm tra trùng
        if (bugAssigneesRepository.existsByBugIdAndUserId(bugId, userId)) {
            throw new DeleteConflictException("User này đã được gán cho bug rồi");
        }

        // Lưu
        BugAssignees assignees = new BugAssignees();
        assignees.setBugId(bugId);
        assignees.setUserId(userId);
        bugAssigneesRepository.save(assignees);

        // Trả về thông tin user
        var user = userService.getUserById(userId);
        return bugAssigneesMapper.toDto(user);
    }

    @Override
    public void unassignUserFromBug(UUID bugId, Integer userId) {
        if (!bugAssigneesRepository.existsByBugIdAndUserId(bugId, userId)) {
            throw new ResourceNotFoundException("User chưa được gán cho bug này");
        }
        bugAssigneesRepository.deleteByBugIdAndUserId(bugId, userId);
    }

    @Override
    public List<BugAssigneesDTO> getAssigneesByBug(UUID bugId) {
        bugService.getBugById(bugId);  // Kiểm tra bug tồn tại

        List<BugAssignees> list = bugAssigneesRepository.findByBugId(bugId);
        return list.stream()
                .map(assignee -> userService.getUserById(assignee.getUserId()))
                .map(bugAssigneesMapper::toDto)
                .toList();
    }
}