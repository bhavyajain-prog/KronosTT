package com.kronostt.service.mapper;

import com.kronostt.engine.model.ScheduledSession;
import com.kronostt.engine.model.TimetablePreferences;
import com.kronostt.web.dto.PreferencesPayloadDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(config = GlobalMapperConfig.class, componentModel = MappingConstants.ComponentModel.SPRING, uses = {SessionMapper.class, RoomMapper.class, SubjectMapper.class, TeacherMapper.class, BatchMapper.class})
public abstract class TimetablePreferencesMapper {

    protected CommonMapperUtil commonMapperUtil;

    @Autowired
    public void setCommonMapperUtil(CommonMapperUtil commonMapperUtil) {
        this.commonMapperUtil = commonMapperUtil;
    }

    public abstract TimetablePreferences toEngine(PreferencesPayloadDto dto);

    public abstract PreferencesPayloadDto toDto(TimetablePreferences preferences);

    public ScheduledSession toEngineSession(PreferencesPayloadDto.PreLockedSessionDto dto) {
        if (dto == null) {
            return null;
        }
        return ScheduledSession.builder().session(null) // Session will be built from IDs by caller
                .weekDay(commonMapperUtil.safeGetWeekDay(dto.getDayOfWeek())).startSlot(dto.getStartSlot()).assignedRoom(null) // Room ID only, resolved by caller
                .build();
    }

    protected PreferencesPayloadDto.PreLockedSessionDto toDtoSession(ScheduledSession session) {
        if (session == null) {
            return null;
        }
        return new PreferencesPayloadDto.PreLockedSessionDto(session.getSession() != null ? session.getSession().getTeacher().getId() : null, session.getSession() != null ? session.getSession().getBatch().getId() : null, session.getSession() != null ? session.getSession().getSubject().getId() : null, session.getAssignedRoom() != null ? session.getAssignedRoom().getId() : null, session.getWeekDay().ordinal(), session.getStartSlot(), session.getSession() != null ? session.getSession().getSlotDuration() : 0);
    }
}
