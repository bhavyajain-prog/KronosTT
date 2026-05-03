package com.kronostt.service.mapper;

import com.kronostt.engine.model.Teacher;
import com.kronostt.persistence.entity.SubjectEntity;
import com.kronostt.persistence.entity.TeacherEntity;
import com.kronostt.web.dto.TeacherDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(config = GlobalMapperConfig.class, componentModel = MappingConstants.ComponentModel.SPRING, uses = SubjectMapper.class)
public interface TeacherMapper {

    // 1. DB <-> Engine POJO
    Teacher toEngine(TeacherEntity entity);

    TeacherEntity toEntity(Teacher pojo);

    // 2. DB <-> Web DTO (This fixes the stream error!)
    @Mapping(target = "subjectIds", source = "subjects")
    TeacherDto toDto(TeacherEntity entity);

    @Mapping(target = "subjects", source = "subjectIds")
    TeacherEntity toEntityFromDto(TeacherDto dto);

    // 3. MapStruct Helpers for flattening/nesting the Subject IDs
    default List<Long> mapSubjectsToIds(List<SubjectEntity> subjects) {
        if (subjects == null) {
            return null;
        }
        return subjects.stream().map(SubjectEntity::getId).toList();
    }

    default List<SubjectEntity> mapIdsToSubjects(List<Long> subjectIds) {
        if (subjectIds == null) {
            return null;
        }
        return subjectIds.stream().map(id -> {
            SubjectEntity subject = new SubjectEntity();
            subject.setId(id);
            return subject;
        }).toList();
    }
}