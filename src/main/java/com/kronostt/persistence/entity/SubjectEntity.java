package com.kronostt.persistence.entity;

import com.kronostt.engine.model.enums.SubjectType;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "subjects")
public class SubjectEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private int weight;

    @Column(nullable = false)
    private int slotDuration;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubjectType subjectType;
}