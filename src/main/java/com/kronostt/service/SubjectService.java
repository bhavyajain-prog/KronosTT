package com.kronostt.service;

import com.kronostt.persistence.entity.SubjectEntity;
import com.kronostt.persistence.repository.SubjectRepository;
import com.kronostt.service.mapper.SubjectMapper;
import com.kronostt.web.dto.SubjectDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final SubjectMapper subjectMapper;

    public List<SubjectDto> getAllSubjects() {
        return subjectRepository.findAll().stream()
                .map(subjectMapper::toDto)
                .toList();
    }

    public SubjectDto getSubjectById(Long id) {
        SubjectEntity entity = subjectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subject not found with ID: " + id));
        return subjectMapper.toDto(entity);
    }

    @Transactional
    public SubjectDto createSubject(SubjectDto dto) {
        SubjectEntity entity = subjectMapper.toEntity(dto);
        SubjectEntity saved = subjectRepository.save(entity);
        return subjectMapper.toDto(saved);
    }

    @Transactional
    public SubjectDto updateSubject(Long id, SubjectDto dto) {
        SubjectEntity existing = subjectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subject not found with ID: " + id));

        // Update fields manually or use a MapStruct @MappingTarget method
        existing.setName(dto.getName());
        existing.setWeight(dto.getWeight());
        existing.setSlotDuration(dto.getSlotDuration());
        existing.setSubjectType(dto.getSubjectType());

        return subjectMapper.toDto(subjectRepository.save(existing));
    }

    @Transactional
    public void deleteSubject(Long id) {
        subjectRepository.deleteById(id);
    }
}