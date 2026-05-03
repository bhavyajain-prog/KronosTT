package com.kronostt.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreferencesPayloadDto {

    // Key: "BatchId_SubjectId", Value: TeacherId
    @Builder.Default
    private Map<String, Long> teacherAllocations = new HashMap<>();

    // Key: Entity ID, Value: RoomId
    @Builder.Default
    private Map<Long, Long> batchHomeRooms = new HashMap<>();

    @Builder.Default
    private Map<Long, Long> subjectRooms = new HashMap<>();

    @Builder.Default
    private Map<Long, Long> teacherRooms = new HashMap<>();

    // Key: SubjectId, Value: List of block durations (e.g., [2, 1, 1])
    @Builder.Default
    private Map<Long, List<Integer>> customSplits = new HashMap<>();

    // Replaced the Engine POJO with a lightweight ID-based DTO
    @Builder.Default
    private List<PreLockedSessionDto> preLockedSessions = new ArrayList<>();

    /**
     * Nested DTO for handling manual overrides from the frontend.
     * The frontend only needs to send the IDs and time coordinates.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PreLockedSessionDto {
        private Long teacherId;
        private Long batchId;
        private Long subjectId;
        private Long roomId;
        private int dayOfWeek;   // e.g., 0 for Monday
        private int startSlot;   // e.g., 0 for 8:00 AM
        private int slotDuration;
    }
}