package com.somamanoj.incidentmanagement.service;

import com.somamanoj.incidentmanagement.dto.AssignRequest;
import com.somamanoj.incidentmanagement.dto.CommentRequest;
import com.somamanoj.incidentmanagement.dto.CommentResponse;
import com.somamanoj.incidentmanagement.dto.DashboardStatsResponse;
import com.somamanoj.incidentmanagement.dto.HistoryResponse;
import com.somamanoj.incidentmanagement.dto.IncidentRequest;
import com.somamanoj.incidentmanagement.dto.IncidentResponse;
import com.somamanoj.incidentmanagement.dto.StatusUpdateRequest;
import com.somamanoj.incidentmanagement.dto.UserSummaryResponse;
import com.somamanoj.incidentmanagement.entity.Incident;
import com.somamanoj.incidentmanagement.entity.IncidentComment;
import com.somamanoj.incidentmanagement.entity.IncidentHistory;
import com.somamanoj.incidentmanagement.entity.IncidentStatus;
import com.somamanoj.incidentmanagement.entity.IssueCategory;
import com.somamanoj.incidentmanagement.entity.Priority;
import com.somamanoj.incidentmanagement.entity.Role;
import com.somamanoj.incidentmanagement.entity.User;
import com.somamanoj.incidentmanagement.exception.BusinessException;
import com.somamanoj.incidentmanagement.exception.ResourceNotFoundException;
import com.somamanoj.incidentmanagement.repository.IncidentCommentRepository;
import com.somamanoj.incidentmanagement.repository.IncidentHistoryRepository;
import com.somamanoj.incidentmanagement.repository.IncidentRepository;
import com.somamanoj.incidentmanagement.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final IncidentCommentRepository commentRepository;
    private final IncidentHistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final UserLookupService userLookupService;
    private final IncidentMapper mapper;

    public IncidentService(
            IncidentRepository incidentRepository,
            IncidentCommentRepository commentRepository,
            IncidentHistoryRepository historyRepository,
            UserRepository userRepository,
            UserLookupService userLookupService,
            IncidentMapper mapper
    ) {
        this.incidentRepository = incidentRepository;
        this.commentRepository = commentRepository;
        this.historyRepository = historyRepository;
        this.userRepository = userRepository;
        this.userLookupService = userLookupService;
        this.mapper = mapper;
    }

    @Transactional
    public IncidentResponse create(IncidentRequest request) {
        User reporter = userLookupService.currentUser();
        Incident incident = Incident.builder()
                .title(request.getTitle().trim())
                .description(request.getDescription().trim())
                .category(request.getCategory())
                .priority(request.getPriority())
                .status(IncidentStatus.OPEN)
                .reporter(reporter)
                .build();
        incidentRepository.save(incident);
        recordHistory(incident, reporter, "CREATED", null, IncidentStatus.OPEN.name(), "Incident reported");
        return mapper.toResponse(incident, List.of());
    }

    @Transactional(readOnly = true)
    public List<IncidentResponse> search(Long id, Priority priority, IncidentStatus status, IssueCategory category) {
        User current = userLookupService.currentUser();
        List<Incident> incidents = incidentRepository.search(id, priority, status, category);

        List<Incident> visible = incidents.stream()
                .filter(incident -> canView(current, incident))
                .toList();

        return visible.stream()
                .map(incident -> mapper.toResponse(incident, List.of()))
                .toList();
    }

    @Transactional(readOnly = true)
    public IncidentResponse getById(Long id) {
        Incident incident = findVisibleIncident(id);
        List<IncidentComment> comments = commentRepository.findByIncidentIdOrderByCreatedAtAsc(id);
        return mapper.toResponse(incident, comments);
    }

    @Transactional
    public IncidentResponse updateStatus(Long id, StatusUpdateRequest request) {
        User current = requireSupportStaff();
        Incident incident = findIncident(id);
        IncidentStatus oldStatus = incident.getStatus();
        IncidentStatus newStatus = request.getStatus();
        validateTransition(oldStatus, newStatus);

        incident.setStatus(newStatus);
        if (newStatus == IncidentStatus.RESOLVED || newStatus == IncidentStatus.CLOSED) {
            incident.setResolvedAt(Optional.ofNullable(incident.getResolvedAt()).orElse(LocalDateTime.now()));
        }
        incidentRepository.save(incident);

        recordHistory(incident, current, "STATUS_CHANGED", oldStatus.name(), newStatus.name(), request.getComment());

        if (request.getComment() != null && !request.getComment().isBlank()) {
            addCommentInternal(incident, current, request.getComment());
        }
        return mapper.toResponse(incident, commentRepository.findByIncidentIdOrderByCreatedAtAsc(id));
    }

    @Transactional
    public IncidentResponse assign(Long id, AssignRequest request) {
        User current = requireSupportStaff();
        Incident incident = findIncident(id);
        User engineer = userRepository.findById(request.getEngineerId())
                .orElseThrow(() -> new ResourceNotFoundException("Support engineer not found"));
        if (engineer.getRole() != Role.SUPPORT_ENGINEER && engineer.getRole() != Role.ADMIN) {
            throw new BusinessException("Incidents can only be assigned to IT support staff");
        }

        String oldAssignee = incident.getAssignee() != null ? incident.getAssignee().getEmail() : null;
        incident.setAssignee(engineer);
        if (incident.getStatus() == IncidentStatus.OPEN) {
            incident.setStatus(IncidentStatus.ASSIGNED);
        }
        incidentRepository.save(incident);
        recordHistory(incident, current, "ASSIGNED", oldAssignee, engineer.getEmail(), "Incident assigned to " + engineer.getFullName());
        return mapper.toResponse(incident, commentRepository.findByIncidentIdOrderByCreatedAtAsc(id));
    }

    @Transactional
    public CommentResponse addComment(Long id, CommentRequest request) {
        User current = requireSupportStaff();
        Incident incident = findIncident(id);
        IncidentComment saved = addCommentInternal(incident, current, request.getComment());
        recordHistory(incident, current, "COMMENT_ADDED", null, null, "Resolution comment added");
        return mapper.toComment(saved);
    }

    @Transactional(readOnly = true)
    public List<HistoryResponse> history(Long id) {
        findVisibleIncident(id);
        return historyRepository.findByIncidentIdOrderByCreatedAtAsc(id).stream()
                .map(mapper::toHistory)
                .toList();
    }

    @Transactional(readOnly = true)
    public DashboardStatsResponse dashboard() {
        User current = userLookupService.currentUser();
        List<Incident> incidents = incidentRepository.findAll().stream()
                .filter(incident -> canView(current, incident))
                .toList();

        Map<IncidentStatus, Long> byStatus = incidents.stream()
                .collect(Collectors.groupingBy(Incident::getStatus, () -> new EnumMap<>(IncidentStatus.class), Collectors.counting()));

        Map<String, Long> byCategory = incidents.stream()
                .collect(Collectors.groupingBy(i -> i.getCategory().name(), Collectors.counting()));

        Map<String, Long> byPriority = incidents.stream()
                .collect(Collectors.groupingBy(i -> i.getPriority().name(), Collectors.counting()));

        long critical = incidents.stream().filter(i -> i.getPriority() == Priority.CRITICAL).count();

        return DashboardStatsResponse.builder()
                .totalIncidents(incidents.size())
                .openIncidents(byStatus.getOrDefault(IncidentStatus.OPEN, 0L) + byStatus.getOrDefault(IncidentStatus.ASSIGNED, 0L))
                .inProgressIncidents(byStatus.getOrDefault(IncidentStatus.IN_PROGRESS, 0L))
                .resolvedIncidents(byStatus.getOrDefault(IncidentStatus.RESOLVED, 0L))
                .closedIncidents(byStatus.getOrDefault(IncidentStatus.CLOSED, 0L))
                .criticalIncidents(critical)
                .incidentsByCategory(byCategory)
                .incidentsByPriority(byPriority)
                .build();
    }

    @Transactional(readOnly = true)
    public List<UserSummaryResponse> supportEngineers() {
        return userRepository.findByRole(Role.SUPPORT_ENGINEER).stream()
                .map(mapper::toUser)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserSummaryResponse> allUsers() {
        return userRepository.findAll().stream()
                .map(mapper::toUser)
                .toList();
    }

    private IncidentComment addCommentInternal(Incident incident, User author, String text) {
        IncidentComment comment = IncidentComment.builder()
                .incident(incident)
                .author(author)
                .comment(text.trim())
                .build();
        return commentRepository.save(comment);
    }

    private void recordHistory(Incident incident, User changedBy, String action, String oldValue, String newValue, String details) {
        IncidentHistory history = IncidentHistory.builder()
                .incident(incident)
                .changedBy(changedBy)
                .action(action)
                .oldValue(oldValue)
                .newValue(newValue)
                .details(details)
                .build();
        historyRepository.save(history);
    }

    private Incident findIncident(Long id) {
        return incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with id " + id));
    }

    private Incident findVisibleIncident(Long id) {
        Incident incident = findIncident(id);
        User current = userLookupService.currentUser();
        if (!canView(current, incident)) {
            throw new AccessDeniedException("You cannot view this incident");
        }
        return incident;
    }

    private boolean canView(User user, Incident incident) {
        if (user.getRole() == Role.ADMIN) {
            return true;
        }
        if (user.getRole() == Role.SUPPORT_ENGINEER) {
            return incident.getAssignee() == null || incident.getAssignee().getId().equals(user.getId());
        }
        return incident.getReporter().getId().equals(user.getId());
    }

    private User requireSupportStaff() {
        User current = userLookupService.currentUser();
        if (current.getRole() != Role.ADMIN && current.getRole() != Role.SUPPORT_ENGINEER) {
            throw new AccessDeniedException("Only IT support staff can perform this action");
        }
        return current;
    }

    private void validateTransition(IncidentStatus from, IncidentStatus to) {
        if (from == to) {
            throw new BusinessException("Incident is already in status " + to);
        }
        boolean allowed = switch (from) {
            case OPEN -> to == IncidentStatus.ASSIGNED || to == IncidentStatus.IN_PROGRESS || to == IncidentStatus.CLOSED;
            case ASSIGNED -> to == IncidentStatus.IN_PROGRESS || to == IncidentStatus.OPEN;
            case IN_PROGRESS -> to == IncidentStatus.RESOLVED || to == IncidentStatus.ASSIGNED;
            case RESOLVED -> to == IncidentStatus.CLOSED || to == IncidentStatus.IN_PROGRESS;
            case CLOSED -> false;
        };
        if (!allowed) {
            throw new BusinessException("Cannot change status from " + from + " to " + to);
        }
    }
}
