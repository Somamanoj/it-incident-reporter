package com.somamanoj.incidentmanagement.repository;

import com.somamanoj.incidentmanagement.entity.Incident;
import com.somamanoj.incidentmanagement.entity.IncidentStatus;
import com.somamanoj.incidentmanagement.entity.IssueCategory;
import com.somamanoj.incidentmanagement.entity.Priority;
import com.somamanoj.incidentmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident, Long> {

    List<Incident> findByReporterOrderByCreatedAtDesc(User reporter);

    List<Incident> findByAssigneeOrderByCreatedAtDesc(User assignee);

    List<Incident> findAllByOrderByCreatedAtDesc();

    @Query("""
            SELECT i FROM Incident i
            WHERE (:id IS NULL OR i.id = :id)
              AND (:priority IS NULL OR i.priority = :priority)
              AND (:status IS NULL OR i.status = :status)
              AND (:category IS NULL OR i.category = :category)
            ORDER BY i.createdAt DESC
            """)
    List<Incident> search(
            @Param("id") Long id,
            @Param("priority") Priority priority,
            @Param("status") IncidentStatus status,
            @Param("category") IssueCategory category
    );
}
