package com.kronostt.engine.algorithm;

import com.kronostt.engine.model.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MultipleSchedulesAlgoTest {

    @Test
    void generateTimeTable() {
        // 1. Define schedule bounds (5 days, 8 slots = 40 total slots per room/teacher)
        int maxSlots = 8;
        int workDays = 5;

        // 2. Load mock data
        List<Room> rooms = dummyRooms();
        List<Teacher> teachers = dummyTeachers();
        List<Batch> batches = dummyBatches();

        // 3. Pre-process: Unpack batches and subjects into a flat List of Sessions
        List<Session> allSessions = buildMockSessions(batches, teachers);

        // 4. Initialize and Run Algorithm
        MultipleSchedulesAlgo algo = new MultipleSchedulesAlgo(allSessions, rooms, workDays, maxSlots);
        ScheduledResult result = algo.generateTimeTable();

        // 5. Output Results for Debugging
        System.out.println("--- TIMETABLE GENERATION RESULTS ---");
        System.out.println("Total Sessions to Schedule: " + allSessions.size());
        System.out.println("Successfully Placed: " + result.getSessions().size());
        System.out.println("Unplaced Sessions: " + result.getUnscheduledSessions().size());

        // 6. Assertions
        assertNotNull(result, "Result should not be null");
        assertTrue(result.getUnscheduledSessions().isEmpty(), "There should be 0 unplaced sessions with ample rooms/slots");
        assertEquals(allSessions.size(), result.getSessions().size(), "All sessions must be successfully scheduled");
    }

    // --- MOCK DATA GENERATORS ---

    private List<Room> dummyRooms() {
        return new ArrayList<>(List.of(
                Room.builder().id(101L).name("Room 101").capacity(60).build(),
                Room.builder().id(102L).name("Room 102").capacity(60).build()
        ));
    }

    private List<Subject> dummySubjects() {
        return new ArrayList<>(List.of(
                Subject.builder().id(1L).name("Mathematics").weight(3).slotDuration(1).build(),
                Subject.builder().id(2L).name("Physics").weight(2).slotDuration(1).build(),
                Subject.builder().id(3L).name("Chemistry").weight(2).slotDuration(1).build(),
                Subject.builder().id(4L).name("Computer Science").weight(3).slotDuration(1).build(),
                Subject.builder().id(7L).name("Data Structures Lab").weight(2).slotDuration(2).build()
        ));
    }

    private List<Teacher> dummyTeachers() {
        List<Subject> subjects = dummySubjects();
        return List.of(
                // Note: Anil teaches Math, Sneha teaches CS. They will be heavily contested!
                Teacher.builder().id(1L).firstName("Anil").lastName("Sharma").subjects(List.of(subjects.get(0))).build(),
                Teacher.builder().id(2L).firstName("Priya").lastName("Mehta").subjects(List.of(subjects.get(1))).build(),
                Teacher.builder().id(3L).firstName("Rohan").lastName("Verma").subjects(List.of(subjects.get(2))).build(),
                Teacher.builder().id(4L).firstName("Sneha").lastName("Joshi").subjects(List.of(subjects.get(3))).build(),
                Teacher.builder().id(7L).firstName("Deepak").lastName("Rajput").subjects(List.of(subjects.get(4))).build()
        );
    }

    private List<Batch> dummyBatches() {
        List<Subject> allSubjects = dummySubjects();
        return new ArrayList<>(List.of(
                Batch.builder().id(1L).name("CSE 2025").section("A").strength(40).subjects(allSubjects).build(),
                Batch.builder().id(2L).name("CSE 2025").section("B").strength(42).subjects(allSubjects).build()
        ));
    }

    // --- PRE-PROCESSOR MOCK ---

    private List<Session> buildMockSessions(List<Batch> batches, List<Teacher> teachers) {
        List<Session> allSessions = new ArrayList<>();
        long sessionIdCounter = 1L;

        for (Batch batch : batches) {
            for (Subject subject : batch.getSubjects()) {

                // Find the teacher who teaches this subject
                Teacher assignedTeacher = teachers.stream()
                        .filter(t -> t.getSubjects().stream().anyMatch(s -> s.getId() == subject.getId()))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("No teacher found for " + subject.getName()));

                // Create individual sessions based on the subject's weight
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