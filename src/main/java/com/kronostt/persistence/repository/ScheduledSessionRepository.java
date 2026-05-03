package com.kronostt.persistence.repository;

import com.kronostt.persistence.entity.ScheduledSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduledSessionRepository extends JpaRepository<ScheduledSessionEntity, Long> {
}
