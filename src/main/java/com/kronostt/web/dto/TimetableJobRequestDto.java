package com.kronostt.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimetableJobRequestDto {
    private PreferencesPayloadDto preferences;
    private String jobName;
    private int weekDays;
    private int maxSlots;
    private int lunchStart;
}
