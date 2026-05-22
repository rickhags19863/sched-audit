package com.schedaudit.dashboard;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ExecutionSummaryTest {

    private static final Instant NOW = Instant.parse("2024-06-01T10:00:00Z");
    private static final Instant NEXT = Instant.parse("2024-06-01T11:00:00Z");

    @Test
    void constructorShouldStoreAllFields() {
        ExecutionSummary summary = new ExecutionSummary("backup-job", 10, 8, 1, 1, NOW, NEXT);

        assertEquals("backup-job", summary.getJobName());
        assertEquals(10, summary.getTotalRuns());
        assertEquals(8, summary.getSuccessfulRuns());
        assertEquals(1, summary.getFailedRuns());
        assertEquals(1, summary.getMissedRuns());
        assertEquals(NOW, summary.getLastExecutionTime());
        assertEquals(NEXT, summary.getNextExpectedTime());
    }

    @Test
    void getSuccessRateShouldReturnCorrectPercentage() {
        ExecutionSummary summary = new ExecutionSummary("report-job", 4, 3, 1, 0, NOW, NEXT);
        assertEquals(75.0, summary.getSuccessRate(), 0.001);
    }

    @Test
    void getSuccessRateShouldReturnZeroWhenNoRuns() {
        ExecutionSummary summary = new ExecutionSummary("new-job", 0, 0, 0, 0, null, NEXT);
        assertEquals(0.0, summary.getSuccessRate(), 0.001);
    }

    @Test
    void constructorShouldThrowOnNullJobName() {
        assertThrows(NullPointerException.class,
                () -> new ExecutionSummary(null, 0, 0, 0, 0, null, null));
    }

    @Test
    void toStringShouldContainJobNameAndSuccessRate() {
        ExecutionSummary summary = new ExecutionSummary("cleanup-job", 10, 10, 0, 0, NOW, NEXT);
        String str = summary.toString();
        assertTrue(str.contains("cleanup-job"));
        assertTrue(str.contains("100.0%"));
    }
}
