package com.kronostt.engine.constraints;

import com.kronostt.engine.TimeTableState;
import com.kronostt.engine.model.Room;
import com.kronostt.engine.model.Session;
import lombok.AllArgsConstructor;

import java.util.Map;
import java.util.Set;

@AllArgsConstructor
public class TeacherUnavailabilityConstraint implements Constraint {

    // Map teacherId with days teacher is not available on the campus
    private final Map<Long, Set<Integer>> unavailableTeachers;

    @Override
    public int evaluate(Session session, Room room, int day, int start, TimeTableState state) {
        long teacherId = session.getTeacher().getId();
        if (unavailableTeachers.containsKey(teacherId)) {
            if (unavailableTeachers.get(teacherId).contains(day)) {
                return -(Integer.MIN_VALUE / 2);
            }
        }
        return 0;
    }
}
