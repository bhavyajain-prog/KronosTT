package com.kronostt.service.mapper;

import com.kronostt.engine.model.Batch;
import com.kronostt.persistence.entity.BatchEntity;
import com.kronostt.persistence.entity.SubjectEntity;
import com.kronostt.web.dto.BatchDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(config = GlobalMapperConfig.class, componentModel = MappingConstants.ComponentModel.SPRING, uses = SubjectMapper.class)
public interface BatchMapper {

    // 1. DB <-> Engine POJO (Used by your TimetableService / Engine)
    Batch toEngine(BatchEntity entity);

    BatchEntity toEntity(Batch pojo);

    // 2. DB <-> Web DTO (Used by your BatchService / REST Controller)
    @Mapping(target = "subjectIds", source = "subjects")
    BatchDto toDto(BatchEntity entity);

    @Mapping(target = "subjects", source = "subjectIds")
    BatchEntity toEntity(BatchDto dto);

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