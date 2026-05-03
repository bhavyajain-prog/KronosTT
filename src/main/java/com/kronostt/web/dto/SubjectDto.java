package com.kronostt.web.dto;

import com.kronostt.engine.model.enums.SubjectType;
import lombok.Data;

@Data
public class SubjectDto {
    private Long id;
    private String name;
    private int weight;
    private int slotDuration;
    private SubjectType subjectType;
}
