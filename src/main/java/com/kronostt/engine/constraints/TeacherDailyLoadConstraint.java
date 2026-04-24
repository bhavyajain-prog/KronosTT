package com.kronostt.engine.constraints;

import com.kronostt.engine.TimeTableState;
import com.kronostt.engine.model.Room;
import com.kronostt.engine.model.Session;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TeacherDailyLoadConstraint implements Constraint {
    private final int maxHoursPerDay;
    private final int penaltyPerHour;

    @Override
    public int evaluate(Session session, Room room, int day, int start, TimeTableState state) {
        long teacherId = session.getTeacher().getId();
        int currentHours = state.getTeacherHoursOnDay(teacherId, day);
        int proposedHours = currentHours + session.getSlotDuration();
        if (proposedHours > maxHoursPerDay) {
            int diff = proposedHours - maxHoursPerDay;
            return -(diff * penaltyPerHour);
        }
        return 0;
    }
}
