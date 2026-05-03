package com.kronostt.web.controller;

import com.kronostt.service.TimetableService;
import com.kronostt.web.dto.TimetableJobRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/timetable")
@RequiredArgsConstructor
public class TimetableController {

    private final TimetableService timetableService;

    /**
     * Endpoint to trigger a new Timetable Generation Run.
     * Expects a JSON payload with the job name and the nested preferences.
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateTimetable(@RequestBody TimetableJobRequestDto request) {
        log.info("Received request to generate timetable for job: {}", request.getJobName());

        // This calls the massive orchestrator method we built earlier
        Long jobId = timetableService.generateTimetable(request);

        // Return a 202 Accepted (because generation might take time in the future)
        // or 201 Created with the Job ID so the frontend can poll for the result.
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(Map.of(
                        "message", "Timetable generation started successfully.",
                        "jobId", jobId
                ));
    }
}