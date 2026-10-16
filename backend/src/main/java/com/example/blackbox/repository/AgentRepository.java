package com.example.blackbox.repository;

import com.example.blackbox.entity.AgentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AgentRepository extends JpaRepository<AgentEntity, String> {
    Optional<AgentEntity> findByName(String name);
}
