package com.kronostt.engine;

import java.util.HashMap;
import java.util.Map;

import com.kronostt.engine.model.Session;

import lombok.Getter;

public class TimeTableState {
    @Getter
    private final int workDays;
    @Getter
    private final int maxSlots;

    private final Map<Long, Session[][]> teacherSchedules = new HashMap<>();
    private final Map<Long, Session[][]> batchSchedules = new HashMap<>();
    private final Map<Long, Session[][]> roomSchedules = new HashMap<>();

    public TimeTableState(int workDays, int maxSlots) {
        this.workDays = workDays;
        this.maxSlots = maxSlots;
    }

    private Session[][] getEmptyGrid() {
        return new Session[workDays][maxSlots];
    }

    public boolean canPlace(Session session, long roomId, int day, int start, int duration) {
        if (start + duration > maxSlots) return false;

        Session[][] teacherBoard = teacherSchedules.getOrDefault(session.getTeacher().getId(), getEmptyGrid());
        Session[][] batchBoard = batchSchedules.getOrDefault(session.getTeacher().getId(), getEmptyGrid());
        Session[][] roomBoard = roomSchedules.getOrDefault(session.getTeacher().getId(), getEmptyGrid());

        for (int i = 0; i < duration; i++) {
            if (teacherBoard[day][start + i] != null ||
                    batchBoard[day][start + i] != null ||
                    roomBoard[day][start + i] != null)
                return false;
        }
        return true;
    }

    public void placeSession(Session session, long roomId, int day, int start, int duration) {
        Session[][] teacherBoard = teacherSchedules.computeIfAbsent(session.getTeacher().getId(), k -> getEmptyGrid());
        Session[][] batchBoard = batchSchedules.computeIfAbsent(session.getTeacher().getId(), k -> getEmptyGrid());
        Session[][] roomBoard = roomSchedules.computeIfAbsent(session.getTeacher().getId(), k -> getEmptyGrid());

        for (int i = 0; i < duration; i++) {
            teacherBoard[day][start + i] = session;
            batchBoard[day][start + i] = session;
            roomBoard[day][start + i] = session;
        }
    }
}