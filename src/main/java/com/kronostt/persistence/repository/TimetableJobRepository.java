package com.kronostt.persistence.repository;

import com.kronostt.persistence.entity.TimetableJobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimetableJobRepository extends JpaRepository<TimetableJobEntity, Long> {
}
