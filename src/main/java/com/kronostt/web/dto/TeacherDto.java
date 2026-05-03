package com.kronostt.web.dto;

import lombok.Data;

import java.util.List;

@Data
public class TeacherDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private List<Long> subjectIds;
}
