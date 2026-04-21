package com.kronostt.engine.algorithm;

import com.kronostt.engine.TimeTableState;
import com.kronostt.engine.model.Room;
import com.kronostt.engine.model.ScheduledSession;
import com.kronostt.engine.model.Session;
import com.kronostt.engine.model.enums.WeekDay;

import java.util.*;

public class MultipleSchedulesAlgo extends AbstractPlacementAlgo {
    private final List<Room> availableRooms;
    private final TimeTableState state;
    private final List<int[]> searchSpace;

    public MultipleSchedulesAlgo(List<Session> sessions, List<Room> availableRooms, int workDays, int slotsPerDay) {
        super(sessions);
        this.availableRooms = availableRooms;
        this.availableRooms.sort(Comparator.comparing(Room::getCapacity));
        this.state = new TimeTableState(workDays, slotsPerDay, slotsPerDay / 2);
        this.searchSpace = buildSearchSpace(workDays, slotsPerDay);
    }

    private List<int[]> buildSearchSpace(int workDays, int slotsPerDay) {
        List<int[]> searchSpace = new ArrayList<>();
        for (int day = 0; day < workDays; day++) {
            for (int slot = 0; slot < slotsPerDay; slot++) {
                searchSpace.add(new int[]{day, slot});
            }
        }
        return searchSpace;
    }

    @Override
    protected boolean attemptPlacing(Session session, List<ScheduledSession> scheduledSessions) {
        int duration = session.getSlotDuration();
        List<int[]> eligiblePositions = new ArrayList<>(searchSpace.stream()
                .filter(pos -> pos[1] <= state.getMaxSlots() - duration)
                .toList());

        Collections.shuffle(eligiblePositions);

        for (int[] position : eligiblePositions) {
            int day = position[0];
            int slot = position[1];
            Room preferredRoom = session.getPreferredRoom();
            if (preferredRoom != null && state.canPlace(session, preferredRoom, day, slot)) {
                state.placeSession(session, preferredRoom, day, slot, duration);
                scheduledSessions.add(ScheduledSession.builder().session(session).weekDay(WeekDay.values()[day]).startSlot(slot).assignedRoom(preferredRoom).build());
                return true;
            }
            for (Room room : availableRooms) {
                // Capacity constraint
                if (room.getCapacity() < session.getBatch().getStrength()) continue;
                // State check
                if (state.canPlace(session, room, day, slot)) {
                    state.placeSession(session, room, day, slot, duration);
                    scheduledSessions.add(ScheduledSession.builder().session(session).weekDay(WeekDay.values()[day]).startSlot(slot).assignedRoom(room).build());
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void sortSessionsByDifficulty() {
        Map<Long, Integer> teacherComponent = new HashMap<>();
        Map<Long, Integer> batchComponent = new HashMap<>();
        for (Session session : sessions) {
            long teacherId = session.getTeacher().getId();
            long batchId = session.getBatch().getId();
            int duration = session.getSlotDuration();

            teacherComponent.merge(teacherId, duration, Integer::sum);
            batchComponent.merge(batchId, duration, Integer::sum);
        }
        sessions.sort(Comparator.comparingInt(Session::getSlotDuration)
                .thenComparingInt(session -> teacherComponent.get(session.getTeacher().getId()))
                .thenComparingInt(session -> batchComponent.get(session.getBatch().getId())).reversed());
    }
}
