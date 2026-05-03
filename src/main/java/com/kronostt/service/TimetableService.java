package com.kronostt.service;

import com.kronostt.engine.SessionGenerator;
import com.kronostt.engine.algorithm.Algo;
import com.kronostt.engine.algorithm.HeuristicPlacementAlgo;
import com.kronostt.engine.model.ScheduledResult;
import com.kronostt.engine.model.TimetablePreferences;
import com.kronostt.persistence.entity.ScheduledSessionEntity;
import com.kronostt.persistence.entity.SessionEntity;
import com.kronostt.persistence.entity.TimetableJobEntity;
import com.kronostt.persistence.repository.*;
import com.kronostt.service.mapper.*;
import com.kronostt.web.dto.TimetableJobRequestDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TimetableService {

    private final TeacherRepository teacherRepository;
    private final TeacherMapper teacherMapper;
    private final BatchRepository batchRepository;
    private final BatchMapper batchMapper;
    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;
    private final TimetablePreferencesMapper timetablePreferencesMapper;
    private final TimetableJobRepository timetableJobRepository;
    private final SessionMapper sessionMapper;
    private final ScheduledSessionMapper scheduledSessionMapper;
    private final SessionRepository sessionRepository;
    private final ScheduledSessionRepository scheduledSessionRepository;

    @Transactional
    public Long generateTimetable(TimetableJobRequestDto request) {
        TimetableJobEntity job = createPendingJob(request);
        try {
            var teachers = teacherRepository.findAll().stream().map(teacherMapper::toEngine).toList();
            var batches = batchRepository.findAll().stream().map(batchMapper::toEngine).toList();
            var rooms = roomRepository.findAll().stream().map(roomMapper::toEngine).toList();

            TimetablePreferences preferences = timetablePreferencesMapper.toEngine(request.getPreferences());
            SessionGenerator generator = new SessionGenerator(teachers, batches, rooms, preferences);
            var generatedSessions = generator.generateAllSessions();

            Algo algo = new HeuristicPlacementAlgo(generatedSessions, preferences.getPreLockedSessions(), rooms, request.getWeekDays(), request.getMaxSlots(), request.getLunchStart(), List.of());

            ScheduledResult result = algo.generateTimeTable();

            saveResults(job, result);
            job.setStatus("COMPLETED");
            timetableJobRepository.save(job);
            return job.getId();
        } catch (Exception e) {
            log.error("Algorithm failed for Job ID: {}", job.getId(), e);
            job.setStatus("FAILED");
            timetableJobRepository.save(job);
            throw new RuntimeException("Timetable generation failed", e);
        }
    }

    private TimetableJobEntity createPendingJob(TimetableJobRequestDto request) {
        TimetableJobEntity job = new TimetableJobEntity();
        job.setName(request.getJobName());
        job.setStatus("PROCESSING");
        job.setPreferences(request.getPreferences());
        return timetableJobRepository.save(job);
    }

    private void saveResults(TimetableJobEntity job, ScheduledResult result) {
        List<SessionEntity> allSessionsToSave = new ArrayList<>();
        List<ScheduledSessionEntity> allScheduledSessionsToSave = new ArrayList<>();

        // Unsuccessful sessions
        for (var unscheduled : result.getUnscheduledSessions()) {
            SessionEntity sessionEntity = sessionMapper.toEntity(unscheduled);
            sessionEntity.setJob(job);
            allSessionsToSave.add(sessionEntity);
        }

        // Successful sessions
        for (var scheduled : result.getSessions()) {
            SessionEntity sessionEntity = sessionMapper.toEntity(scheduled.getSession());
            sessionEntity.setJob(job);
            allSessionsToSave.add(sessionEntity);

            ScheduledSessionEntity scheduledSessionEntity = scheduledSessionMapper.toEntity(scheduled);
            scheduledSessionEntity.setJob(job);
            scheduledSessionEntity.setSession(sessionEntity);
            allScheduledSessionsToSave.add(scheduledSessionEntity);
        }

        sessionRepository.saveAll(allSessionsToSave);
        scheduledSessionRepository.saveAll(allScheduledSessionsToSave);

        job.setGeneratedSessions(allScheduledSessionsToSave);
        job.setRemainingSessions(allSessionsToSave);
    }

}
