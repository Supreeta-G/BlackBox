package com.example.blackbox.repository;

import com.example.blackbox.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, String> {
    List<TaskEntity> findByAgentIdOrderByCreatedAtDesc(String agentId);
    List<TaskEntity> findAllByOrderByCreatedAtDesc();
}
