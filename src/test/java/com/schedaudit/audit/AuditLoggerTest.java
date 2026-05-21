package com.schedaudit.audit;

import com.schedaudit.model.JobExecution;
import com.schedaudit.repository.JobExecutionRepository;
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
class AuditLoggerTest {

    @Mock
    private JobExecutionRepository repository;

    private AuditLogger auditLogger;

    @BeforeEach
    void setUp() {
        auditLogger = new AuditLogger(repository);
    }

    @Test
    void constructor_nullRepository_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> new AuditLogger(null));
    }

    @Test
    void record_validExecution_savesAndLogs() {
        JobExecution execution = new JobExecution("backup-job", Instant.now(), "SUCCESS");
        auditLogger.record(execution);
        verify(repository, times(1)).save(execution);
    }

    @Test
    void record_nullExecution_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> auditLogger.record(null));
        verifyNoInteractions(repository);
    }

    @Test
    void getHistory_validJobName_returnsHistory() {
        String jobName = "cleanup-job";
        List<JobExecution> expected = List.of(
                new JobExecution(jobName, Instant.now().minusSeconds(120), "SUCCESS"),
                new JobExecution(jobName, Instant.now(), "SUCCESS")
        );
        when(repository.findByJobName(jobName)).thenReturn(expected);

        List<JobExecution> result = auditLogger.getHistory(jobName);

        assertEquals(2, result.size());
        verify(repository).findByJobName(jobName);
    }

    @Test
    void getHistory_blankJobName_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> auditLogger.getHistory("  "));
        verifyNoInteractions(repository);
    }

    @Test
    void getHistoryBetween_validRange_returnsFilteredHistory() {
        String jobName = "report-job";
        Instant from = Instant.now().minusSeconds(3600);
        Instant to = Instant.now();
        List<JobExecution> expected = List.of(
                new JobExecution(jobName, from.plusSeconds(600), "FAILED")
        );
        when(repository.findByJobNameAndTimeRange(jobName, from, to)).thenReturn(expected);

        List<JobExecution> result = auditLogger.getHistoryBetween(jobName, from, to);

        assertEquals(1, result.size());
        assertEquals("FAILED", result.get(0).getStatus());
    }

    @Test
    void getHistoryBetween_fromAfterTo_throwsException() {
        Instant from = Instant.now();
        Instant to = from.minusSeconds(60);
        assertThrows(IllegalArgumentException.class,
                () -> auditLogger.getHistoryBetween("any-job", from, to));
    }

    @Test
    void auditReport_summarisesExecutionsCorrectly() {
        String jobName = "sync-job";
        List<JobExecution> executions = List.of(
                new JobExecution(jobName, Instant.now().minusSeconds(200), "SUCCESS"),
                new JobExecution(jobName, Instant.now().minusSeconds(100), "FAILED"),
                new JobExecution(jobName, Instant.now(), "SUCCESS")
        );
        AuditReport report = new AuditReport(jobName, executions);

        assertEquals(jobName, report.getJobName());
        assertEquals(3, report.getTotalRuns());
        assertEquals(2L, report.getCountByStatus("SUCCESS"));
        assertEquals(1L, report.getCountByStatus("FAILED"));
        assertEquals(0L, report.getCountByStatus("SKIPPED"));
    }
}
