package com.example.blackbox.repository;

import com.example.blackbox.entity.PolicyDecisionEntity;
import com.example.blackbox.enums.PolicyResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PolicyDecisionRepository extends JpaRepository<PolicyDecisionEntity, String> {
    List<PolicyDecisionEntity> findByExecutionIdOrderByEvaluatedAtAsc(String executionId);
    long countByDecision(PolicyResult decision);
}
