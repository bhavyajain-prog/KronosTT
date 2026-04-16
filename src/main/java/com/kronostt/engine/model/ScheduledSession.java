package com.kronostt.engine.model;

import com.kronostt.engine.model.enums.WeekDay;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ScheduledSession {
    private Session session;
    private WeekDay weekDay;
    private int startSlot;
    private Room assignedRoom;
}
