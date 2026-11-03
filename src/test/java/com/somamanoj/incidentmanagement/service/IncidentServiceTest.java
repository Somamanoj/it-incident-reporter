package com.somamanoj.incidentmanagement.service;

import com.somamanoj.incidentmanagement.dto.IncidentRequest;
import com.somamanoj.incidentmanagement.dto.IncidentResponse;
import com.somamanoj.incidentmanagement.entity.Incident;
import com.somamanoj.incidentmanagement.entity.IncidentStatus;
import com.somamanoj.incidentmanagement.entity.IssueCategory;
import com.somamanoj.incidentmanagement.entity.Priority;
import com.somamanoj.incidentmanagement.entity.Role;
import com.somamanoj.incidentmanagement.entity.User;
import com.somamanoj.incidentmanagement.repository.IncidentCommentRepository;
import com.somamanoj.incidentmanagement.repository.IncidentHistoryRepository;
import com.somamanoj.incidentmanagement.repository.IncidentRepository;
import com.somamanoj.incidentmanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IncidentServiceTest {

    @Mock
    private IncidentRepository incidentRepository;
    @Mock
    private IncidentCommentRepository commentRepository;
    @Mock
    private IncidentHistoryRepository historyRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserLookupService userLookupService;

    private IncidentService incidentService;

    @BeforeEach
    void setUp() {
        incidentService = new IncidentService(
                incidentRepository,
                commentRepository,
                historyRepository,
                userRepository,
                userLookupService,
                new IncidentMapper()
        );
    }

    @Test
    void createIncidentStartsInOpenStatus() {
        User employee = User.builder()
                .id(10L)
                .fullName("Demo Employee")
                .email("employee@company.com")
                .role(Role.EMPLOYEE)
                .build();
        when(userLookupService.currentUser()).thenReturn(employee);
        when(incidentRepository.save(any(Incident.class))).thenAnswer(invocation -> {
            Incident incident = invocation.getArgument(0);
            incident.setId(1L);
            return incident;
        });

        IncidentRequest request = new IncidentRequest();
        request.setTitle("Laptop not starting");
        request.setDescription("Black screen after power on");
        request.setCategory(IssueCategory.HARDWARE);
        request.setPriority(Priority.HIGH);

        IncidentResponse response = incidentService.create(request);

        assertThat(response.getStatus()).isEqualTo(IncidentStatus.OPEN);
        assertThat(response.getTitle()).isEqualTo("Laptop not starting");
        assertThat(response.getReporter().getEmail()).isEqualTo("employee@company.com");

        ArgumentCaptor<Incident> captor = ArgumentCaptor.forClass(Incident.class);
        verify(incidentRepository).save(captor.capture());
        assertThat(captor.getValue().getPriority()).isEqualTo(Priority.HIGH);
        verify(historyRepository).save(any());
    }
}
