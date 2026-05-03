package com.kronostt.service;

import com.kronostt.persistence.entity.TeacherEntity;
import com.kronostt.persistence.repository.TeacherRepository;
import com.kronostt.service.mapper.TeacherMapper;
import com.kronostt.web.dto.TeacherDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final TeacherMapper teacherMapper; // Maps Entity <-> DTO

    public List<TeacherDto> getAllTeachers() {
        return teacherRepository.findAll().stream()
                .map(teacherMapper::toDto) // Assumes you added toDto in the mapper
                .toList();
    }

    public TeacherDto getTeacherById(Long id) {
        TeacherEntity entity = teacherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        return teacherMapper.toDto(entity);
    }

    @Transactional
    public TeacherDto createTeacher(TeacherDto dto) {
        TeacherEntity entity = teacherMapper.toEntityFromDto(dto);
        TeacherEntity saved = teacherRepository.save(entity);
        return teacherMapper.toDto(saved);
    }

    @Transactional
    public void deleteTeacher(Long id) {
        // Add business validation here later!
        teacherRepository.deleteById(id);
    }
}