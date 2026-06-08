package com.schoolmoney.service;

import com.schoolmoney.dto.ChildRequest;
import com.schoolmoney.model.Child;
import com.schoolmoney.model.SchoolClass;
import com.schoolmoney.repository.ChildRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChildService {

    private final ChildRepository childRepository;
    private final SchoolClassService schoolClassService;

    public Child addChild(ChildRequest request, String parentId) {
        Child child = Child.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .avatarUrl(request.getAvatarUrl())
                .dateOfBirth(request.getDateOfBirth())
                .parentId(parentId)
                .build();
        return childRepository.save(child);
    }

    public List<Child> getMyChildren(String parentId) {
        return childRepository.findByParentId(parentId);
    }

    public Child assignToClass(String childId, String inviteToken, String parentId) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new RuntimeException("Child not found"));

        if (!child.getParentId().equals(parentId)) {
            throw new RuntimeException("Not your child");
        }

        SchoolClass schoolClass = schoolClassService.getClassByToken(inviteToken);
        child.setClassId(schoolClass.getId());

        return childRepository.save(child);
    }
}