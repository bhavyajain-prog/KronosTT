package com.kronostt.engine.algorithm;

import com.kronostt.engine.model.*;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Same full-load dataset as {@link ConstraintPropagationAlgoFullLoadTest}, exercised against
 * {@link MultipleSchedulesAlgo} (greedy shuffle placement + shared {@link com.kronostt.engine.TimeTableState}).
 */
class MultipleSchedulesAlgoFullLoadTest {

    private static final int WORK_DAYS = 6;
    private static final int MAX_SLOTS = 8;

    private static final int NUM_BATCHES = 64;
    private static final int NUM_ROOMS = 85;
    private static final int NUM_SUBJECTS = 33;
    private static final int NUM_TEACHERS = 99;

    @Test
    void testFullLoadTimeTableGeneration() {
        List<Room> rooms = generateRooms();
        List<Subject> subjects = generateSubjects();
        List<Teacher> teachers = generateTeachers(subjects);
        List<Batch> batches = generateBatches(subjects);

        List<Session> allSessions = buildMockSessions(batches, teachers);

        int totalSlotHours = allSessions.stream()
                .mapToInt(Session::getSlotDuration)
                .sum();
        int batchSlotHours = totalSlotHours / NUM_BATCHES;
        int teacherAvgLoad = totalSlotHours / NUM_TEACHERS;

        System.out.println("=== MULTIPLE SCHEDULES ALGO — FULL LOAD (same data as CP full load) ===");
        System.out.printf("Time Grid: %d days × %d slots%n", WORK_DAYS, MAX_SLOTS);
        System.out.println("-------------------------------------------");
        System.out.println("Resources:");
        System.out.println("  Batches: " + batches.size());
        System.out.println("  Subjects: " + subjects.size());
        System.out.println("  Teachers: " + teachers.size());
        System.out.println("  Rooms: " + rooms.size());
        System.out.println("-------------------------------------------");
        System.out.println("Session Load:");
        System.out.println("  Total Sessions: " + allSessions.size());
        System.out.println("  Total Slot-Hours: " + totalSlotHours);
        System.out.println("  Avg per Batch: " + batchSlotHours + " slot-hours/week");
        System.out.println("  Avg per Teacher: " + teacherAvgLoad + " slot-hours/week");
        System.out.println("  Avg per Room: " + (totalSlotHours / NUM_ROOMS) + " slot-hours/week");
        System.out.println("-------------------------------------------");

        long startTime = System.currentTimeMillis();
        MultipleSchedulesAlgo algo = new MultipleSchedulesAlgo(
                allSessions, new ArrayList<>(), rooms, WORK_DAYS, MAX_SLOTS);
        ScheduledResult result = algo.generateTimeTable();
        long endTime = System.currentTimeMillis();

        int placed = result.getSessions().size();
        int unplaced = result.getUnscheduledSessions().size();
        double successRate = (placed * 100.0) / allSessions.size();

        System.out.println("RESULTS:");
        System.out.println("  Sessions Placed: " + placed);
        System.out.println("  Sessions Failed: " + unplaced);
        System.out.printf("  Success Rate: %.2f%%%n", successRate);
        System.out.println("  Time Taken: " + (endTime - startTime) + "ms");
        System.out.println("-------------------------------------------");

        printDistributionAnalysis(result, rooms);

        assertNotNull(result);
        assertEquals(0, unplaced,
                "Expected 100% placement but " + unplaced + " sessions could not be scheduled.");
        assertEquals(allSessions.size(), placed);
    }

    private void printDistributionAnalysis(ScheduledResult result, List<Room> rooms) {
        System.out.println("DISTRIBUTION ANALYSIS:");

        Map<String, Long> byDay = result.getSessions().stream()
                .collect(Collectors.groupingBy(s -> s.getWeekDay().name(), Collectors.counting()));
        System.out.println("  Sessions by Day:");
        byDay.forEach((day, count) ->
                System.out.printf("    %s: %d sessions%n", day, count));

        Map<Long, Long> roomUsage = result.getSessions().stream()
                .collect(Collectors.groupingBy(s -> s.getAssignedRoom().getId(), Collectors.counting()));
        long avgRoomUse = result.getSessions().size() / rooms.size();
        long maxRoomUse = roomUsage.values().stream().mapToLong(Long::longValue).max().orElse(0);
        long minRoomUse = roomUsage.values().stream().mapToLong(Long::longValue).min().orElse(0);
        System.out.printf("  Room Utilization: avg=%d, min=%d, max=%d%n",
                avgRoomUse, minRoomUse, maxRoomUse);

        System.out.println("-------------------------------------------");
    }

    private List<Room> generateRooms() {
        List<Room> rooms = new ArrayList<>();
        for (long i = 0; i < NUM_ROOMS; i++) {
            int capacity = (i % 10 == 0) ? 120 : 60;
            rooms.add(Room.builder()
                    .id(i)
                    .name("Room_" + i + (capacity == 120 ? "_LARGE" : ""))
                    .capacity(capacity)
                    .build());
        }
        return rooms;
    }

    private List<Subject> generateSubjects() {
        List<Subject> subjects = new ArrayList<>();
        for (long i = 0; i < NUM_SUBJECTS; i++) {
            boolean isLab = (i % 3 == 0);
            subjects.add(Subject.builder()
                    .id(i)
                    .name("Sub_" + i + (isLab ? "_LAB" : "_LEC"))
                    .weight(isLab ? 2 : 3)
                    .slotDuration(isLab ? 2 : 1)
                    .build());
        }
        return subjects;
    }

    private List<Teacher> generateTeachers(List<Subject> subjects) {
        List<Teacher> teachers = new ArrayList<>();
        for (int i = 0; i < NUM_TEACHERS; i++) {
            Subject assignedSubject = subjects.get(i % subjects.size());
            teachers.add(Teacher.builder()
                    .id(i)
                    .firstName("Teach_" + i)
                    .lastName("")
                    .subjects(List.of(assignedSubject))
                    .build());
        }
        return teachers;
    }

    private List<Batch> generateBatches(List<Subject> allSubjects) {
        List<Batch> batches = new ArrayList<>();
        for (long i = 0; i < NUM_BATCHES; i++) {
            List<Subject> batchSubjects = new ArrayList<>();

            int lectureOffset = (int) (i % (allSubjects.size() - 10));
            int labOffset = 20 + (int) (i % 10);

            List<Subject> lectures = allSubjects.stream()
                    .filter(s -> s.getSlotDuration() == 1)
                    .toList();
            List<Subject> labs = allSubjects.stream()
                    .filter(s -> s.getSlotDuration() == 2)
                    .toList();

            for (int j = 0; j < 4 && j < lectures.size(); j++) {
                int idx = (lectureOffset + j) % lectures.size();
                batchSubjects.add(lectures.get(idx));
            }

            for (int j = 0; j < 2 && j < labs.size(); j++) {
                int idx = (labOffset + j) % labs.size();
                batchSubjects.add(labs.get(idx));
            }

            int batchSize = (i % 4 == 0) ? 100 : 60;

            batches.add(Batch.builder()
                    .id(i)
                    .name("Batch_" + i + "_" + (batchSize == 100 ? "L" : "S"))
                    .strength(batchSize)
                    .subjects(batchSubjects)
                    .build());
        }
        return batches;
    }

    private List<Session> buildMockSessions(List<Batch> batches, List<Teacher> teachers) {
        List<Session> allSessions = new ArrayList<>();
        long sessionIdCounter = 1L;
        Map<Long, Integer> teacherLoad = new HashMap<>();

        for (Batch batch : batches) {
            for (Subject subject : batch.getSubjects()) {
                List<Teacher> eligibleTeachers = teachers.stream()
                        .filter(t -> t.getSubjects().stream()
                                .anyMatch(s -> s.getId() == subject.getId()))
                        .toList();

                if (eligibleTeachers.isEmpty()) {
                    throw new RuntimeException("No teacher found for " + subject.getName());
                }

                Teacher assignedTeacher = eligibleTeachers.stream()
                        .min(Comparator.comparingInt(t -> teacherLoad.getOrDefault(t.getId(), 0)))
                        .orElseThrow();

                for (int i = 0; i < subject.getWeight(); i++) {
                    allSessions.add(Session.builder()
                            .id(sessionIdCounter++)
                            .subject(subject)
                            .teacher(assignedTeacher)
                            .batch(batch)
                            .slotDuration(subject.getSlotDuration())
                            .build());
                }

                teacherLoad.merge(assignedTeacher.getId(), subject.getWeight(), Integer::sum);
            }
        }
        return allSessions;
    }
}
