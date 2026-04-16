package com.kronostt.engine.algorithm;

import com.kronostt.engine.model.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class BaseAlgoTest {

    @Test
    void testGenerateTimeTable() {
        int maxSlots = 6;
        int workDays = 6;
        List<Teacher> teachers = dummyTeachers();
        Batch batch = dummyBatch();
        List<Room> rooms = dummyRooms();

        BaseAlgo algo = new BaseAlgo(maxSlots, workDays, teachers, batch, rooms, false);
        ScheduledResult result = algo.generateTimeTable();

        System.out.println(result);
    }

    private List<Room> dummyRooms() {
        return new ArrayList<>(List.of(
                Room.builder()
                        .name("Room 1")
                        .capacity(60)
                        .build()
        ));
    }

    private List<Subject> dummySubjects() {
        return new ArrayList<>(List.of(
                Subject.builder().id(1L).name("Mathematics").weight(3).slotDuration(1).build(),
                Subject.builder().id(2L).name("Physics").weight(2).slotDuration(1).build(),
                Subject.builder().id(3L).name("Chemistry").weight(2).slotDuration(1).build(),
                Subject.builder().id(4L).name("Computer Science").weight(3).slotDuration(1).build(),
                Subject.builder().id(5L).name("English").weight(1).slotDuration(1).build(),
                Subject.builder().id(6L).name("Physical Education").weight(1).slotDuration(1).build(),
                Subject.builder().id(7L).name("Data Structures Lab").weight(2).slotDuration(2).build()
        ));
    }

    private List<Teacher> dummyTeachers() {
        List<Subject> subjects = dummySubjects();

        return List.of(
                Teacher.builder().id(1L).firstName("Anil").lastName("Sharma").email("anil.sharma@kronos.edu")
                        .subjects(new ArrayList<>(List.of(subjects.get(0)))).build(),
                Teacher.builder().id(2L).firstName("Priya").lastName("Mehta").email("priya.mehta@kronos.edu")
                        .subjects(new ArrayList<>(List.of(subjects.get(1)))).build(),
                Teacher.builder().id(3L).firstName("Rohan").lastName("Verma").email("rohan.verma@kronos.edu")
                        .subjects(new ArrayList<>(List.of(subjects.get(2)))).build(),
                Teacher.builder().id(4L).firstName("Sneha").lastName("Joshi").email("sneha.joshi@kronos.edu")
                        .subjects(new ArrayList<>(List.of(subjects.get(3)))).build(),
                Teacher.builder().id(5L).firstName("Karan").lastName("Gupta").email("karan.gupta@kronos.edu")
                        .subjects(new ArrayList<>(List.of(subjects.get(4)))).build(),
                Teacher.builder().id(6L).firstName("Meena").lastName("Tiwari").email("meena.tiwari@kronos.edu")
                        .subjects(new ArrayList<>(List.of(subjects.get(5)))).build(),
                Teacher.builder().id(7L).firstName("Deepak").lastName("Rajput").email("deepak.rajput@kronos.edu")
                        .subjects(new ArrayList<>(List.of(subjects.get(6)))).build()
        );
    }

    private Batch dummyBatch() {
        return Batch.builder()
                .id(1L)
                .name("CSE 2025")
                .section("A")
                .strength(40)
                .subjects(dummySubjects())
                .build();
    }
}
