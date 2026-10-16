package com.example.blackbox.repository;

import com.example.blackbox.entity.AgentEventEntity;
import com.example.blackbox.enums.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AgentEventRepository extends JpaRepository<AgentEventEntity, String> {

    List<AgentEventEntity> findByExecutionIdOrderBySequenceNumberAsc(String executionId);

    Optional<AgentEventEntity> findTopByExecutionIdOrderBySequenceNumberDesc(String executionId);

    Optional<AgentEventEntity> findByExecutionIdAndSequenceNumber(String executionId, Integer sequenceNumber);

    long countByExecutionId(String executionId);

    long countByIsTamperedTrue();

    @Query("SELECT e FROM AgentEventEntity e WHERE e.executionId = :executionId " +
           "AND (:eventType IS NULL OR e.eventType = :eventType) " +
           "AND (:status IS NULL OR e.status = :status) " +
           "AND (:toolName IS NULL OR e.toolName = :toolName) " +
           "ORDER BY e.sequenceNumber ASC")
    List<AgentEventEntity> findFilteredEvents(
            @Param("executionId") String executionId,
            @Param("eventType") EventType eventType,
            @Param("status") String status,
            @Param("toolName") String toolName
    );

    List<AgentEventEntity> findByStatusOrderByTimestampDesc(String status);
}
