package com.schoolmoney.service;

import com.schoolmoney.dto.ClassRequest;
import com.schoolmoney.model.SchoolClass;
import com.schoolmoney.repository.SchoolClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SchoolClassService {

    private final SchoolClassRepository schoolClassRepository;

    public SchoolClass createClass(ClassRequest request, String userId) {
        if (schoolClassRepository.existsByName(request.getName())) {
            throw new RuntimeException("Class name already exists");
        }

        SchoolClass schoolClass = SchoolClass.builder()
                .name(request.getName())
                .treasurerId(userId)
                .inviteToken(UUID.randomUUID().toString())
                .build();

        return schoolClassRepository.save(schoolClass);
    }

    public SchoolClass getClassByToken(String token) {
        return schoolClassRepository.findByInviteToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid invite token"));
    }

    public List<SchoolClass> getMyClasses(String userId) {
        return schoolClassRepository.findByTreasurerId(userId);
    }
}