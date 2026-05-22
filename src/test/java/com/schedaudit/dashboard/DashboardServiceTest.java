package com.schedaudit.dashboard;

import com.schedaudit.model.JobExecution;
import com.schedaudit.repository.JobExecutionRepository;
import com.schedaudit.scheduler.ScheduleRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock private JobExecutionRepository repository;
    @Mock private ScheduleRegistry scheduleRegistry;

    private DashboardService dashboardService;

    private static final Instant T1 = Instant.parse("2024-06-01T09:00:00Z");
    private static final Instant T2 = Instant.parse("2024-06-01T10:00:00Z");
    private static final Instant NEXT = Instant.parse("2024-06-01T11:00:00Z");

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardService(repository, scheduleRegistry);
    }

    @Test
    void getSummaryForJobShouldAggregateStatusCounts() {
        JobExecution e1 = mockExecution("nightly-job", "SUCCESS", T1);
        JobExecution e2 = mockExecution("nightly-job", "FAILED", T2);
        when(repository.findByJobName("nightly-job")).thenReturn(List.of(e1, e2));
        when(scheduleRegistry.getNextExpectedTime("nightly-job")).thenReturn(NEXT);

        ExecutionSummary summary = dashboardService.getSummaryForJob("nightly-job");

        assertEquals("nightly-job", summary.getJobName());
        assertEquals(2, summary.getTotalRuns());
        assertEquals(1, summary.getSuccessfulRuns());
        assertEquals(1, summary.getFailedRuns());
        assertEquals(0, summary.getMissedRuns());
        assertEquals(T2, summary.getLastExecutionTime());
        assertEquals(NEXT, summary.getNextExpectedTime());
    }

    @Test
    void getSummaryForJobShouldHandleEmptyHistory() {
        when(repository.findByJobName("new-job")).thenReturn(List.of());
        when(scheduleRegistry.getNextExpectedTime("new-job")).thenReturn(NEXT);

        ExecutionSummary summary = dashboardService.getSummaryForJob("new-job");

        assertEquals(0, summary.getTotalRuns());
        assertNull(summary.getLastExecutionTime());
        assertEquals(0.0, summary.getSuccessRate(), 0.001);
    }

    @Test
    void getAllSummariesShouldReturnOnePerRegisteredJob() {
        when(scheduleRegistry.getRegisteredJobNames()).thenReturn(List.of("job-a", "job-b"));
        when(repository.findByJobName(anyString())).thenReturn(List.of());
        when(scheduleRegistry.getNextExpectedTime(anyString())).thenReturn(NEXT);

        List<ExecutionSummary> summaries = dashboardService.getAllSummaries();

        assertEquals(2, summaries.size());
    }

    @Test
    void constructorShouldThrowOnNullRepository() {
        assertThrows(NullPointerException.class,
                () -> new DashboardService(null, scheduleRegistry));
    }

    private JobExecution mockExecution(String jobName, String status, Instant time) {
        JobExecution e = mock(JobExecution.class);
        when(e.getJobName()).thenReturn(jobName);
        when(e.getStatus()).thenReturn(status);
        when(e.getExecutionTime()).thenReturn(time);
        return e;
    }
}
