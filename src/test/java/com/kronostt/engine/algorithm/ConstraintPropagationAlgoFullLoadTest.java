package com.kronostt.engine.algorithm;

import com.kronostt.engine.model.*;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Full-load test designed for 100% placement feasibility.
 * <p>
 * Configuration:
 * - 6 days × 8 slots = 48 slots (minus lunch = 42 usable slots/day/resource)<br/>
 * - 12 batches × 6 subjects = 72 subject assignments<br/>
 * - ~1080 sessions (balanced load)<br/>
 * - 80 rooms (sufficient capacity)<br/>
 * - 36 teachers (3 teachers per subject, spread load)
 * </p>
 * This is designed to be tight but achievable - like a real university timetable.
 */
class ConstraintPropagationAlgoFullLoadTest {

    // Time structure: 6 days, 8 slots per day, lunch at slot 3
    private static final int WORK_DAYS = 6;
    private static final int MAX_SLOTS = 8;
    private static final int LUNCH_SLOT = MAX_SLOTS / 2 - 1; // slot 3 is lunch

    // Resources - scaled for 1000+ sessions with balanced load
    // Tuned to ensure 100% placement feasibility with slight slack
    private static final int NUM_BATCHES = 64;           // 64 batches (reduced by 1)
    private static final int NUM_ROOMS = 85;             // 85 rooms (1 extra for slack)
    private static final int NUM_SUBJECTS = 33;          // 33 subjects
    private static final int NUM_TEACHERS = 99;          // 99 teachers (3 per subject)

    // Session distribution
    private static final int SUBJECTS_PER_BATCH = 6;

    @Test
    void testFullLoadTimeTableGeneration() {
        // 1. Generate Data
        List<Room> rooms = generateRooms();
        List<Subject> subjects = generateSubjects();
        List<Teacher> teachers = generateTeachers(subjects);
        List<Batch> batches = generateBatches(subjects);

        // 2. Build Sessions
        List<Session> allSessions = buildMockSessions(batches, teachers);

        // Calculate theoretical load
        int totalSlotHours = allSessions.stream()
                .mapToInt(Session::getSlotDuration)
                .sum();
        int batchSlotHours = totalSlotHours / NUM_BATCHES;
        int teacherAvgLoad = totalSlotHours / NUM_TEACHERS;

        System.out.println("=== FULL LOAD TIMETABLE CONFIGURATION ===");
        System.out.printf("Time Grid: %d days × %d slots = %d slots/day%n",
                WORK_DAYS, MAX_SLOTS - 1, (MAX_SLOTS - 1) * WORK_DAYS);
        System.out.printf("Usable slots (minus lunch slot %d): %d slots/week/resource%n",
                LUNCH_SLOT, (MAX_SLOTS - 1) * WORK_DAYS - WORK_DAYS);
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

        // 3. Run Algorithm
        long startTime = System.currentTimeMillis();
        ConstraintPropagationAlgo algo = new ConstraintPropagationAlgo(
                allSessions, null, rooms, WORK_DAYS, MAX_SLOTS, LUNCH_SLOT, null);
        ScheduledResult result = algo.generateTimeTable();
        long endTime = System.currentTimeMillis();

        // 4. Results
        int placed = result.getSessions().size();
        int unplaced = result.getUnscheduledSessions().size();
        double successRate = (placed * 100.0) / allSessions.size();

        System.out.println("RESULTS:");
        System.out.println("  Sessions Placed: " + placed);
        System.out.println("  Sessions Failed: " + unplaced);
        System.out.printf("  Success Rate: %.2f%%%n", successRate);
        System.out.println("  Time Taken: " + (endTime - startTime) + "ms");
        System.out.println("-------------------------------------------");

        // Distribution analysis
        printDistributionAnalysis(result, batches, teachers, rooms);

        // 5. Assertions - expect 100% placement
        assertNotNull(result);
        assertEquals(0, unplaced,
                "Expected 100% placement but " + unplaced + " sessions could not be scheduled. " +
                        "This indicates the data may have impossible constraints or the algorithm needs tuning.");
        assertEquals(allSessions.size(), placed);
    }

    private void printDistributionAnalysis(ScheduledResult result, List<Batch> batches,
                                           List<Teacher> teachers, List<Room> rooms) {
        System.out.println("DISTRIBUTION ANALYSIS:");

        // By day
        Map<String, Long> byDay = result.getSessions().stream()
                .collect(Collectors.groupingBy(s -> s.getWeekDay().name(), Collectors.counting()));
        System.out.println("  Sessions by Day:");
        byDay.forEach((day, count) ->
                System.out.printf("    %s: %d sessions%n", day, count));

        // Room utilization
        Map<Long, Long> roomUsage = result.getSessions().stream()
                .collect(Collectors.groupingBy(s -> s.getAssignedRoom().getId(), Collectors.counting()));
        long avgRoomUse = result.getSessions().size() / rooms.size();
        long maxRoomUse = roomUsage.values().stream().mapToLong(Long::longValue).max().orElse(0);
        long minRoomUse = roomUsage.values().stream().mapToLong(Long::longValue).min().orElse(0);
        System.out.printf("  Room Utilization: avg=%d, min=%d, max=%d%n",
                avgRoomUse, minRoomUse, maxRoomUse);

        System.out.println("-------------------------------------------");
    }

    // --- DATA GENERATORS ---

    private List<Room> generateRooms() {
        List<Room> rooms = new ArrayList<>();
        // Mix of room sizes - most are standard 60-seat, some larger for big batches
        for (long i = 0; i < NUM_ROOMS; i++) {
            int capacity = (i % 10 == 0) ? 120 : 60; // Every 10th room is large
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
        // 18 subjects: mix of lectures (duration 1, weight 3) and labs (duration 2, weight 2)
        for (long i = 0; i < NUM_SUBJECTS; i++) {
            boolean isLab = (i % 3 == 0); // Every 3rd subject is a lab (6 labs, 12 lectures)
            subjects.add(Subject.builder()
                    .id(i)
                    .name("Sub_" + i + (isLab ? "_LAB" : "_LEC"))
                    .weight(isLab ? 2 : 3)        // Labs: 2 sessions/week, Lectures: 3 sessions/week
                    .slotDuration(isLab ? 2 : 1)  // Labs: 2 slots, Lectures: 1 slot
                    .build());
        }
        return subjects;
    }

    private List<Teacher> generateTeachers(List<Subject> subjects) {
        List<Teacher> teachers = new ArrayList<>();
        // 90 teachers: each subject has 3 teachers
        // Each teacher teaches only 1 subject (simplifies assignment, reduces contention)
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

        // 60 batches, each taking 6 subjects from 30 available
        // Each batch gets 2 labs + 4 lectures, with even distribution
        for (long i = 0; i < NUM_BATCHES; i++) {
            List<Subject> batchSubjects = new ArrayList<>();

            // Distribute subjects evenly: each batch gets subjects at offset positions
            // This ensures good spread without too much overlap
            int lectureOffset = (int) (i % (allSubjects.size() - 10)); // Lectures from first ~20
            int labOffset = 20 + (int) (i % 10); // Labs from last 10

            // Add 4 lectures (duration 1)
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

            // Add 2 labs
            for (int j = 0; j < 2 && j < labs.size(); j++) {
                int idx = (labOffset + j) % labs.size();
                batchSubjects.add(labs.get(idx));
            }

            int batchSize = (i % 4 == 0) ? 100 : 60; // Mix of batch sizes

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

        // Track teacher loads to distribute evenly
        Map<Long, Integer> teacherLoad = new HashMap<>();

        for (Batch batch : batches) {
            for (Subject subject : batch.getSubjects()) {
                // Find all teachers for this subject
                List<Teacher> eligibleTeachers = teachers.stream()
                        .filter(t -> t.getSubjects().stream()
                                .anyMatch(s -> s.getId() == subject.getId()))
                        .toList();

                if (eligibleTeachers.isEmpty()) {
                    throw new RuntimeException("No teacher found for " + subject.getName());
                }

                // Pick teacher with lowest current load (round-robin distribution)
                Teacher assignedTeacher = eligibleTeachers.stream()
                        .min(Comparator.comparingInt(t -> teacherLoad.getOrDefault(t.getId(), 0)))
                        .orElseThrow();

                // Create sessions based on subject weight
                for (int i = 0; i < subject.getWeight(); i++) {
                    allSessions.add(Session.builder()
                            .id(sessionIdCounter++)
                            .subject(subject)
                            .teacher(assignedTeacher)
                            .batch(batch)
                            .slotDuration(subject.getSlotDuration())
                            .build());
                }

                // Update teacher load
                teacherLoad.merge(assignedTeacher.getId(), subject.getWeight(), Integer::sum);
            }
        }
        return allSessions;
    }
}
