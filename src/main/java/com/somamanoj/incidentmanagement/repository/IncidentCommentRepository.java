package com.somamanoj.incidentmanagement.repository;

import com.somamanoj.incidentmanagement.entity.IncidentComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidentCommentRepository extends JpaRepository<IncidentComment, Long> {

    List<IncidentComment> findByIncidentIdOrderByCreatedAtAsc(Long incidentId);
}
