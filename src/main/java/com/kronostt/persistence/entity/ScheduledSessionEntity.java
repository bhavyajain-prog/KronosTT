package com.kronostt.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "scheduled_sessions")
public class ScheduledSessionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "job_id", nullable = false)
    private TimetableJobEntity job;

    @OneToOne(fetch = FetchType.LAZY)
    private SessionEntity session;

    @Column(nullable = false)
    private int dayOfWeek;

    @Column(nullable = false)
    private int startSlot;

    @ManyToOne(fetch = FetchType.LAZY)
    private RoomEntity room;
}
