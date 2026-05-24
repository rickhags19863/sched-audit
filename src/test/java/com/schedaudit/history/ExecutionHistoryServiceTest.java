package com.schedaudit.history;

import com.schedaudit.model.JobExecution;
import com.schedaudit.repository.JobExecutionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExecutionHistoryServiceTest {

    private JobExecutionRepository repository;
    private ExecutionHistoryService service;

    private static final Instant T1 = Instant.parse("2024-01-01T10:00:00Z");
    private static final Instant T2 = Instant.parse("2024-01-01T11:00:00Z");
    private static final Instant T3 = Instant.parse("2024-01-01T12:00:00Z");

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(JobExecutionRepository.class);
        service = new ExecutionHistoryService(repository);
    }

    @Test
    void constructorRejectsNullRepository() {
        assertThrows(IllegalArgumentException.class, () -> new ExecutionHistoryService(null));
    }

    @Test
    void getHistoryReturnsFilteredAndSortedResults() {
        JobExecution e1 = jobExecution("backup", T1, "SUCCESS");
        JobExecution e2 = jobExecution("backup", T2, "FAILED");
        JobExecution e3 = jobExecution("backup", T3, "SUCCESS");
        when(repository.findByJobName("backup")).thenReturn(Arrays.asList(e3, e1, e2));

        List<JobExecution> result = service.getHistory("backup", T1, T2);

        assertEquals(2, result.size());
        assertEquals(T1, result.get(0).getStartTime());
        assertEquals(T2, result.get(1).getStartTime());
    }

    @Test
    void getHistoryThrowsOnInvalidRange() {
        assertThrows(IllegalArgumentException.class,
                () -> service.getHistory("backup", T3, T1));
    }

    @Test
    void getHistoryThrowsOnBlankJobName() {
        assertThrows(IllegalArgumentException.class,
                () -> service.getHistory("  ", T1, T3));
    }

    @Test
    void getLatestPerJobReturnsNewestExecution() {
        JobExecution old = jobExecution("cleanup", T1, "SUCCESS");
        JobExecution newer = jobExecution("cleanup", T3, "SUCCESS");
        when(repository.findAll()).thenReturn(Arrays.asList(old, newer));

        Map<String, JobExecution> latest = service.getLatestPerJob();

        assertEquals(1, latest.size());
        assertEquals(T3, latest.get("cleanup").getStartTime());
    }

    @Test
    void getStatusBreakdownCountsCorrectly() {
        when(repository.findByJobName("report")).thenReturn(Arrays.asList(
                jobExecution("report", T1, "SUCCESS"),
                jobExecution("report", T2, "SUCCESS"),
                jobExecution("report", T3, "FAILED")
        ));

        Map<String, Long> breakdown = service.getStatusBreakdown("report");

        assertEquals(2L, breakdown.get("SUCCESS"));
        assertEquals(1L, breakdown.get("FAILED"));
    }

    @Test
    void getTrackedJobNamesReturnsSortedDistinct() {
        when(repository.findAll()).thenReturn(Arrays.asList(
                jobExecution("zebra", T1, "SUCCESS"),
                jobExecution("alpha", T2, "SUCCESS"),
                jobExecution("zebra", T3, "FAILED")
        ));

        List<String> names = service.getTrackedJobNames();

        assertEquals(Arrays.asList("alpha", "zebra"), names);
    }

    @Test
    void getHistoryReturnsEmptyWhenNoMatches() {
        when(repository.findByJobName("ghost")).thenReturn(Collections.emptyList());
        List<JobExecution> result = service.getHistory("ghost", T1, T3);
        assertTrue(result.isEmpty());
    }

    private JobExecution jobExecution(String name, Instant startTime, String status) {
        JobExecution e = new JobExecution();
        e.setJobName(name);
        e.setStartTime(startTime);
        e.setStatus(status);
        return e;
    }
}
