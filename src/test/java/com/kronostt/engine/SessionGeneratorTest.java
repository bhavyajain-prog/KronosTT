package com.kronostt.engine;

import com.kronostt.engine.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SessionGeneratorTest {

    private List<Teacher> teachers;
    private List<Batch> batches;
    private List<Room> rooms;
    private TimetablePreferences prefs;

    @BeforeEach
    void setUp() {
        // 1. Setup Base Data
        Subject math = Subject.builder().id(1L).name("Math").weight(3).slotDuration(1).build();
        Subject physicsLab = Subject.builder().id(2L).name("Physics Lab").weight(1).slotDuration(2).build();

        Teacher anil = Teacher.builder().id(10L).firstName("Anil").subjects(List.of(math)).build();
        Teacher priya = Teacher.builder().id(20L).firstName("Priya").subjects(List.of(math, physicsLab)).build();
        teachers = List.of(anil, priya);

        rooms = List.of(
                Room.builder().id(101L).name("CSE Home Room").build(),
                Room.builder().id(201L).name("Physics Lab Room").build(),
                Room.builder().id(301L).name("Anil's Lair").build()
        );

        Batch cseA = Batch.builder().id(1L).name("CSE-A").subjects(List.of(math, physicsLab)).build();
        batches = List.of(cseA);

        prefs = new TimetablePreferences();
    }

    @Test
    void testDefaultGenerationNoPreferences() {
        SessionGenerator generator = new SessionGenerator(teachers, batches, rooms, prefs);
        List<Session> sessions = generator.generateAllSessions();

        // Total weight is 3 (Math) + 1 (Lab) = 4 sessions expected if standard split
        assertEquals(4, sessions.size(), "Should generate 3 Math sessions and 1 Lab session");

        long mathCount = sessions.stream().filter(s -> s.getSubject().getName().equals("Math")).count();
        assertEquals(3, mathCount, "Math should be split into 3 sessions based on its weight");
    }

    @Test
    void testTeacherAllocationOverride() {
        // Math is taught by both Anil and Priya. Let's force it to Priya for CSE-A.
        prefs.getTeacherAllocations().put("1_1", 20L); // BatchId_SubjectId -> TeacherId (Priya)

        SessionGenerator generator = new SessionGenerator(teachers, batches, rooms, prefs);
        List<Session> sessions = generator.generateAllSessions();

        sessions.stream()
                .filter(s -> s.getSubject().getName().equals("Math"))
                .forEach(s -> assertEquals(20L, s.getTeacher().getId(), "Teacher override failed"));
    }

    @Test
    void testRoomHierarchyResolution() {
        // Scenario:
        // 1. CSE-A has a home room (101)
        // 2. Anil has a specific lecture hall (301)
        // 3. Physics Lab must happen in the Lab (201)
        prefs.getBatchHomeRooms().put(1L, 101L);
        prefs.getTeacherRooms().put(10L, 301L);
        prefs.getSubjectRooms().put(2L, 201L); // Subject overrides everything

        // Force Anil to teach Math so we can test his room preference
        prefs.getTeacherAllocations().put("1_1", 10L);

        SessionGenerator generator = new SessionGenerator(teachers, batches, rooms, prefs);
        List<Session> sessions = generator.generateAllSessions();

        for (Session session : sessions) {
            if (session.getSubject().getName().equals("Physics Lab")) {
                assertEquals(201L, session.getPreferredRoom().getId(), "Subject room override failed");
            } else if (session.getSubject().getName().equals("Math")) {
                // Anil teaches Math, and Teacher Room > Batch Room
                assertEquals(301L, session.getPreferredRoom().getId(), "Teacher room override failed");
            }
        }
    }

    @Test
    void testCustomDurationSplits() {
        // Normally, Math is three 1-hour sessions (weight 3, duration 1).
        // Let's override it to be one 2-hour session and one 1-hour session.
        prefs.getCustomSplits().put(1L, List.of(2, 1));

        SessionGenerator generator = new SessionGenerator(teachers, batches, rooms, prefs);
        List<Session> sessions = generator.generateAllSessions();

        List<Session> mathSessions = sessions.stream()
                .filter(s -> s.getSubject().getName().equals("Math"))
                .toList();

        assertEquals(2, mathSessions.size(), "Should only generate 2 sessions based on custom split");
        assertEquals(2, mathSessions.get(0).getSlotDuration(), "First math session should be 2 hours");
        assertEquals(1, mathSessions.get(1).getSlotDuration(), "Second math session should be 1 hour");
    }
}