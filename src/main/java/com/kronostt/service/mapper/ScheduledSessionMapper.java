package com.kronostt.service.mapper;

import com.kronostt.engine.model.ScheduledSession;
import com.kronostt.persistence.entity.ScheduledSessionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(config = GlobalMapperConfig.class, componentModel = MappingConstants.ComponentModel.SPRING, uses = {SessionMapper.class, RoomMapper.class})
public interface ScheduledSessionMapper {

    @Mapping(target = "weekDay", source = "dayOfWeek")
    @Mapping(target = "assignedRoom", source = "room")
    ScheduledSession toEngine(ScheduledSessionEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "job", ignore = true)
    @Mapping(target = "dayOfWeek", source = "weekDay")
    @Mapping(target = "room", source = "assignedRoom")
    ScheduledSessionEntity toEntity(ScheduledSession pojo);
}
