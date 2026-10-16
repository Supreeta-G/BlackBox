package com.example.blackbox.repository;

import com.example.blackbox.entity.ToolCallEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ToolCallRepository extends JpaRepository<ToolCallEntity, String> {
    List<ToolCallEntity> findByExecutionIdOrderByStartedAtAsc(String executionId);
    long countByToolName(String toolName);
}
