package com.kronostt.service.mapper;

import com.kronostt.engine.model.Room;
import com.kronostt.persistence.entity.RoomEntity;
import com.kronostt.web.dto.RoomDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(config = GlobalMapperConfig.class, componentModel = MappingConstants.ComponentModel.SPRING, uses = BatchMapper.class)
public interface RoomMapper {

    // 1. DB <-> Engine POJO (Used by your TimetableService / Engine)
    Room toEngine(RoomEntity entity);

    RoomEntity toEntity(Room pojo);

    // 2. DB <-> Web DTO (Used by your RoomService / REST Controller)

    // Extracts the ID from the nested BatchEntity to keep the JSON flat
    @Mapping(target = "fixedBatchId", source = "fixedBatch.id")
    RoomDto toDto(RoomEntity entity);

    // Ignored because RoomService handles the database lookup for the Batch!
    @Mapping(target = "fixedBatch", ignore = true)
    RoomEntity toEntity(RoomDto dto);
}