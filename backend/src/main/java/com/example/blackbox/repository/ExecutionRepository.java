package com.example.blackbox.repository;

import com.example.blackbox.entity.ExecutionEntity;
import com.example.blackbox.enums.ExecutionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExecutionRepository extends JpaRepository<ExecutionEntity, String> {
    List<ExecutionEntity> findAllByOrderByStartedAtDesc();
    List<ExecutionEntity> findByTaskIdOrderByStartedAtDesc(String taskId);
    List<ExecutionEntity> findByParentExecutionIdOrderByStartedAtAsc(String parentExecutionId);
    List<ExecutionEntity> findByRootExecutionIdOrderByStartedAtAsc(String rootExecutionId);
    long countByStatus(ExecutionStatus status);
}
