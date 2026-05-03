package com.kronostt.persistence.entity;

import com.kronostt.web.dto.PreferencesPayloadDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(name = "timetable_jobs")
@Setter
@Getter
public class TimetableJobEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String status;

    @JdbcTypeCode(SqlTypes.JSON)
    private PreferencesPayloadDto preferences;

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SessionEntity> remainingSessions;

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ScheduledSessionEntity> generatedSessions;
}
