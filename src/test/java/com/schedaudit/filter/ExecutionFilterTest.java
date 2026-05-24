package com.schedaudit.filter;

import com.schedaudit.model.JobExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExecutionFilterTest {

    private JobExecution successJob;
    private JobExecution failedJob;
    private JobExecution missedJob;
    private List<JobExecution> allExecutions;

    @BeforeEach
    void setUp() {
        Instant base = Instant.parse("2024-06-01T10:00:00Z");

        successJob = new JobExecution("job-a", "SUCCESS", base);
        failedJob  = new JobExecution("job-b", "FAILURE", base.plusSeconds(3600));
        missedJob  = new JobExecution("job-a", "MISSED",  base.plusSeconds(7200));

        allExecutions = List.of(successJob, failedJob, missedJob);
    }

    @Test
    void filterByJobName_returnsOnlyMatchingJob() {
        ExecutionFilter filter = new ExecutionFilter().withJobName("job-a");
        List<JobExecution> result = filter.apply(allExecutions);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(e -> "job-a".equals(e.getJobName())));
    }

    @Test
    void filterByStatus_returnsOnlyFailures() {
        ExecutionFilter filter = new ExecutionFilter().withStatus(ExecutionFilter.Status.FAILURE);
        List<JobExecution> result = filter.apply(allExecutions);
        assertEquals(1, result.size());
        assertEquals("FAILURE", result.get(0).getStatus());
    }

    @Test
    void filterByTimeRange_returnsExecutionsWithinRange() {
        Instant from = Instant.parse("2024-06-01T10:30:00Z");
        Instant to   = Instant.parse("2024-06-01T12:30:00Z");
        ExecutionFilter filter = new ExecutionFilter().withFrom(from).withTo(to);
        List<JobExecution> result = filter.apply(allExecutions);
        assertEquals(2, result.size());
        assertFalse(result.contains(successJob));
    }

    @Test
    void filterWithNoConstraints_returnsAll() {
        ExecutionFilter filter = new ExecutionFilter();
        List<JobExecution> result = filter.apply(allExecutions);
        assertEquals(3, result.size());
    }

    @Test
    void filterOnNullList_returnsEmpty() {
        ExecutionFilter filter = new ExecutionFilter().withJobName("job-a");
        List<JobExecution> result = filter.apply(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void filterByJobNameAndStatus_returnsCombinedMatch() {
        ExecutionFilter filter = new ExecutionFilter()
                .withJobName("job-a")
                .withStatus(ExecutionFilter.Status.MISSED);
        List<JobExecution> result = filter.apply(allExecutions);
        assertEquals(1, result.size());
        assertEquals(missedJob, result.get(0));
    }
}
