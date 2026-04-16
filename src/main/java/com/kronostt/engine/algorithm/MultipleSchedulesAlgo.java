package com.kronostt.engine.algorithm;

import com.kronostt.engine.TimeTableState;
import com.kronostt.engine.model.Room;
import com.kronostt.engine.model.ScheduledResult;
import com.kronostt.engine.model.ScheduledSession;
import com.kronostt.engine.model.Session;
import com.kronostt.engine.model.enums.WeekDay;

import java.util.*;

public class MultipleSchedulesAlgo implements Algo {
    private final List<Session> sessions;
    private final List<Room> availableRooms;
    private final TimeTableState state;

    public MultipleSchedulesAlgo(List<Session> sessions, List<Room> availableRooms, int workDays, int slotsPerDay) {
        this.sessions = sessions;
        this.availableRooms = availableRooms;
        this.state = new TimeTableState(workDays, slotsPerDay);
    }

    @Override
    public ScheduledResult generateTimeTable() {
        // Step 1: Create all the sessions for all the batches all at once - already in input

        // Step 2: Sorting sessions on basis of 3 comparators
        //          1. Session Length
        //          2. Teacher work hours
        //          3. Batch Weight
        sortSessionsByDifficulty();

        // Step 3: Placement into Timetable
        List<ScheduledSession> scheduledSessions = new ArrayList<>();
        List<Session> unscheduledSessions = new ArrayList<>();
        for (Session session : sessions) {
            boolean isPlaced = attemptPlacing(session, scheduledSessions);
            if (!isPlaced) unscheduledSessions.add(session);
        }
        return ScheduledResult.builder().sessions(scheduledSessions).unscheduledSessions(unscheduledSessions).build();
    }

    private boolean attemptPlacing(Session session, List<ScheduledSession> scheduledSessions) {
        int duration = session.getSlotDuration();
        List<int[]> eligiblePositions = new ArrayList<>(state.getWorkDays() * (state.getMaxSlots() - duration));
        for (int day = 0; day < state.getWorkDays(); day++) {
            for (int slot = 0; slot < state.getMaxSlots() - duration + 1; slot++) {
                eligiblePositions.add(new int[]{day, slot});
            }
        }
        Collections.shuffle(eligiblePositions);
        for (int[] position : eligiblePositions) {
            int day = position[0];
            int slot = position[1];
            Room preferredRoom = session.getPreferredRoom();
            if (preferredRoom != null && state.canPlace(session, preferredRoom.getId(), day, slot, duration)) {
                state.placeSession(session, preferredRoom.getId(), day, slot, duration);
                scheduledSessions.add(ScheduledSession.builder().session(session).weekDay(WeekDay.values()[day]).startSlot(slot).assignedRoom(preferredRoom).build());
                return true;
            }
            for (Room room : availableRooms) {
                // Capacity constraint
                if (room.getCapacity() < session.getBatch().getStrength()) continue;
                // State check
                if (state.canPlace(session, room.getId(), day, slot, duration)) {
                    state.placeSession(session, room.getId(), day, slot, duration);
                    scheduledSessions.add(ScheduledSession.builder().session(session).weekDay(WeekDay.values()[day]).startSlot(slot).assignedRoom(room).build());
                    return true;
                }
            }
        }
        return false;
    }

    private void sortSessionsByDifficulty() {
        Map<Long, Integer> teacherComponent = new HashMap<>();
        Map<Long, Integer> batchComponent = new HashMap<>();
        for (Session session : sessions) {
            long teacherId = session.getTeacher().getId();
            long batchId = session.getBatch().getId();
            int duration = session.getSlotDuration();

            teacherComponent.merge(teacherId, duration, Integer::sum);
            batchComponent.merge(batchId, duration, Integer::sum);
        }
        sessions.sort(Comparator.comparingInt(Session::getSlotDuration).thenComparingInt(session -> teacherComponent.get(session.getTeacher().getId())).thenComparingInt(session -> batchComponent.get(session.getBatch().getId())).reversed());
    }
}
