package com.kronostt.service.mapper;

import com.kronostt.engine.model.Subject;
import com.kronostt.persistence.entity.SubjectEntity;
import com.kronostt.web.dto.SubjectDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(config = GlobalMapperConfig.class, componentModel = MappingConstants.ComponentModel.SPRING)
public interface SubjectMapper {

    // 1. DB <-> Engine POJO (Used by your TimetableService / Engine)
    Subject toEngine(SubjectEntity entity);

    SubjectEntity toEntity(Subject pojo);

    // 2. DB <-> Web DTO (Used by your SubjectService / REST Controller)
    SubjectDto toDto(SubjectEntity entity);

    SubjectEntity toEntity(SubjectDto dto);
}