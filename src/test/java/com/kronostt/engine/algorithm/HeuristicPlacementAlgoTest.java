package com.kronostt.engine.algorithm;

import com.kronostt.engine.model.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class HeuristicPlacementAlgoTest {
    // Test volume
    private static final int WORK_DAYS = 6;
    private static final int MAX_SLOTS = 8;
    private static final int NUM_ROOMS = 63;
    private static final int NUM_SUBJECTS = 15;
    private static final int NUM_TEACHERS = 102;
    private static final int NUM_BATCHES = 63;
    private static final int SUBJECTS_PER_BATCH = 6;

    @Test
    void testLargeScaleTimeTableGeneration() {
        // 1. Generate Programmatic Data
        List<Room> rooms = generateRooms();
        List<Subject> subjects = generateSubjects();
        List<Teacher> teachers = generateTeachers(subjects);
        List<Batch> batches = generateBatches(subjects);

        // 2. Pre-process into Sessions
        List<Session> allSessions = buildMockSessions(batches, teachers);

        // 3. Initialize Engine
        HeuristicPlacementAlgo algo = new HeuristicPlacementAlgo(allSessions, rooms, WORK_DAYS, MAX_SLOTS, MAX_SLOTS / 2 - 1);
        ScheduledResult result = algo.generateTimeTable();

        // 4. Output Statistics
        System.out.println("=== STRESS TEST RESULTS ===");
        System.out.println("Total Rooms: " + rooms.size());
        System.out.println("Total Teachers: " + teachers.size());
        System.out.println("Total Batches: " + batches.size());
        System.out.println("---------------------------");
        System.out.println("Total Sessions Generated: " + allSessions.size());
        System.out.println("Sessions Successfully Placed: " + result.getSessions().size());
        System.out.println("Sessions Failed (Unplaced): " + result.getUnscheduledSessions().size());

        // Let's print out the first 5 scheduled sessions just to see the data mapping
        System.out.println("---------------------------");
        System.out.println("Sample Placements:");
        result.getSessions().stream().limit(5).forEach(s ->
                System.out.printf("Batch: %s | %s | %s | %s | Day: %s, Slot: %d%n",
                        s.getSession().getBatch().getName(),
                        s.getSession().getSubject().getName(),
                        s.getSession().getTeacher().getFirstName(),
                        s.getAssignedRoom().getName(),
                        s.getWeekDay(),
                        s.getStartSlot()
                )
        );

        // 5. Assertions
        assertNotNull(result);
        assertEquals(0, result.getUnscheduledSessions().size(),
                "The algorithm failed to place " + result.getUnscheduledSessions().size() + " sessions.");
        assertEquals(allSessions.size(), result.getSessions().size());
    }

    // --- PROGRAMMATIC DATA GENERATORS ---

    private List<Room> generateRooms() {
        List<Room> rooms = new ArrayList<>();
        for (long i = 0; i < HeuristicPlacementAlgoTest.NUM_ROOMS; i++) {
            rooms.add(Room.builder()
                    .id(i)
                    .name("Room_" + i)
                    .capacity(100) // Huge capacity so we only test time/teacher collisions, not capacity limits right now
                    .build());
        }
        return rooms;
    }

    private List<Subject> generateSubjects() {
        List<Subject> subjects = new ArrayList<>();
        for (long i = 0; i < HeuristicPlacementAlgoTest.NUM_SUBJECTS; i++) {
            boolean isLab = (i % 4 == 0); // Every 4th subject is a lab
            subjects.add(Subject.builder()
                    .id(i)
                    .name("Sub_" + i + (isLab ? "_LAB" : "_LEC"))
                    .weight(isLab ? 2 : 3)        // Labs happen 2 times a week, Lectures 3 times
                    .slotDuration(isLab ? 2 : 1)  // Labs take 2 hours, Lectures 1 hour
                    .build());
        }
        return subjects;
    }

    private List<Teacher> generateTeachers(List<Subject> subjects) {
        List<Teacher> teachers = new ArrayList<>();
        for (int i = 0; i < HeuristicPlacementAlgoTest.NUM_TEACHERS; i++) {
            // ith Teacher teaches ith Subject strictly
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
        for (long i = 0; i < HeuristicPlacementAlgoTest.NUM_BATCHES; i++) {
            List<Subject> batchSubjects = new ArrayList<>();
            // Sliding window assignment: Batch 0 gets Sub 0-5, Batch 1 gets Sub 1-6, etc.
            // This creates heavy competition for specific teachers.
            for (int j = 0; j < SUBJECTS_PER_BATCH; j++) {
                int subjectIndex = (int) ((i + j) % allSubjects.size());
                batchSubjects.add(allSubjects.get(subjectIndex));
            }

            batches.add(Batch.builder()
                    .id(i)
                    .name("Batch_" + i)
                    .strength(60)
                    .subjects(batchSubjects)
                    .build());
        }
        return batches;
    }

    // --- PRE-PROCESSOR MOCK ---

    private List<Session> buildMockSessions(List<Batch> batches, List<Teacher> teachers) {
        List<Session> allSessions = new ArrayList<>();
        long sessionIdCounter = 1L;

        for (Batch batch : batches) {
            for (Subject subject : batch.getSubjects()) {

                Teacher assignedTeacher = teachers.stream()
                        .filter(t -> t.getSubjects().stream().anyMatch(s -> s.getId() == subject.getId()))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("No teacher found for " + subject.getName()));

                for (int i = 0; i < subject.getWeight(); i++) {
                    allSessions.add(Session.builder()
                            .id(sessionIdCounter++)
                            .subject(subject)
                            .teacher(assignedTeacher)
                            .batch(batch)
                            .slotDuration(subject.getSlotDuration())
                            .build());
                }
            }
        }
        return allSessions;
    }
}