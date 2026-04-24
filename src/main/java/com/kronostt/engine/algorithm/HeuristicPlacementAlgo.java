package com.kronostt.engine.algorithm;

import com.kronostt.engine.TimeTableState;
import com.kronostt.engine.constraints.Constraint;
import com.kronostt.engine.model.Room;
import com.kronostt.engine.model.ScheduledSession;
import com.kronostt.engine.model.Session;
import com.kronostt.engine.model.enums.WeekDay;

import java.util.*;

public class HeuristicPlacementAlgo extends AbstractPlacementAlgo {
    private static final int MAX_EVALUATIONS = 10;
    private static final int GOOD_ENOUGH_SCORE = 5;
    private final TimeTableState state;
    private final List<Room> rooms;
    private final List<int[]> baseSearchBase;
    private final List<Constraint> constraints;

    public HeuristicPlacementAlgo(List<Session> sessions, List<ScheduledSession> lockedSessions, List<Room> rooms, int weekDays, int maxSlots, int lunchStart, List<Constraint> constraints) {

        // 1. Pass to super, defaulting to empty list if lockedSessions is null
        super(sessions, lockedSessions != null ? lockedSessions : new ArrayList<>());

        this.rooms = rooms;

        // 2. Default constraints to empty list if null
        this.constraints = constraints != null ? constraints : new ArrayList<>();

        // 3. Initialize state
        this.state = new TimeTableState(weekDays, maxSlots, lunchStart);

        // 4. Build search space exactly ONCE
        this.baseSearchBase = new ArrayList<>();
        for (int d = 0; d < weekDays; d++) {
            for (int s = 0; s < maxSlots; s++) {
                baseSearchBase.add(new int[]{d, s});
            }
        }
    }

    @Override
    protected boolean attemptPlacing(Session session, List<ScheduledSession> scheduledSessions) {
        int duration = session.getSlotDuration();
        List<int[]> eligiblePositions = new ArrayList<>(baseSearchBase.stream().filter(pos -> pos[1] <= state.getMaxSlots() - duration).toList());
        Collections.shuffle(eligiblePositions);

        int bestScore = Integer.MIN_VALUE;
        int[] bestPosition = null;
        Room bestRoom = null;
        int evaluations = 0;
        for (int[] pos : eligiblePositions) {
            int day = pos[0];
            int start = pos[1];

            // Preferred Room
            Room prefferedRoom = session.getPreferredRoom();
            if (prefferedRoom != null && state.canPlace(session, prefferedRoom, day, start)) {
                int score = state.getHeuristicScore(session, prefferedRoom, day, start, constraints);
                if (score >= GOOD_ENOUGH_SCORE) {
                    executePlacement(session, prefferedRoom, day, start, scheduledSessions);
                    return true;
                }
                if (score > bestScore) {
                    bestScore = score;
                    bestPosition = pos;
                    bestRoom = prefferedRoom;
                }
                evaluations++;
            }

            // Fallback Rooms
            rooms.sort(Comparator.comparingInt(Room::getCapacity));
            for (Room room : rooms) {
                if (prefferedRoom != null && prefferedRoom.getId() == room.getId()) continue;
                if (room.getCapacity() < session.getBatch().getStrength()) continue;

                if (state.canPlace(session, room, day, start)) {
                    int score = state.getHeuristicScore(session, room, day, start, constraints);
                    evaluations++;
                    if (score >= GOOD_ENOUGH_SCORE) {
                        executePlacement(session, room, day, start, scheduledSessions);
                        return true;
                    }
                    if (score > bestScore) {
                        bestScore = score;
                        bestPosition = pos;
                        bestRoom = room;
                    }

                    if (bestRoom != null && evaluations >= MAX_EVALUATIONS) {
                        executePlacement(session, bestRoom, bestPosition[0], bestPosition[1], scheduledSessions);
                        return true;
                    }
                }
            }
        }
        if (bestRoom != null) {
            executePlacement(session, bestRoom, bestPosition[0], bestPosition[1], scheduledSessions);
            return true;
        }
        return false;
    }

    private void executePlacement(Session session, Room room, int day, int start, List<ScheduledSession> scheduledSessions) {
        state.placeSession(session, room, day, start, session.getSlotDuration());
        scheduledSessions.add(ScheduledSession.builder().session(session).startSlot(start).weekDay(WeekDay.values()[day]).assignedRoom(room).build());
    }

    @Override
    protected void sortSessionsByDifficulty() {
        // Get teacher work hours
        Map<Long, Integer> teacherDifficultyMap = new HashMap<>();
        // Get batch requirement hours
        Map<Long, Integer> batchDifficultyMap = new HashMap<>();

        // Fill the maps
        for (Session session : sessions) {
            long teacherId = session.getTeacher().getId();
            long batchId = session.getBatch().getId();
            int duration = session.getSlotDuration();

            teacherDifficultyMap.put(teacherId, duration + teacherDifficultyMap.getOrDefault(teacherId, 0));
            batchDifficultyMap.put(batchId, duration + batchDifficultyMap.getOrDefault(batchId, 0));
        }

        // Create the chained comparator
        sessions.sort(Comparator.comparingInt(Session::getSlotDuration).thenComparingInt(session -> teacherDifficultyMap.getOrDefault(session.getTeacher().getId(), 0)).thenComparingInt(session -> batchDifficultyMap.getOrDefault(session.getBatch().getId(), 0)).reversed());
    }
}
