package com.schedaudit.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class JobExecutionTest {

    private static final Instant SCHEDULED = Instant.parse("2024-06-01T10:00:00Z");
    private static final Instant EXECUTED  = Instant.parse("2024-06-01T10:00:05Z");

    @Test
    void shouldCreateSuccessfulExecution() {
        JobExecution exec = new JobExecution("backup-job", SCHEDULED, EXECUTED,
                JobExecution.Status.SUCCESS, "Completed in 5s");

        assertEquals("backup-job", exec.getJobName());
        assertEquals(SCHEDULED, exec.getScheduledAt());
        assertEquals(EXECUTED, exec.getExecutedAt());
        assertEquals(JobExecution.Status.SUCCESS, exec.getStatus());
        assertFalse(exec.isMissed());
    }

    @Test
    void shouldIdentifyMissedExecution() {
        JobExecution exec = new JobExecution("cleanup-job", SCHEDULED, null,
                JobExecution.Status.MISSED, "No execution recorded within window");

        assertTrue(exec.isMissed());
        assertNull(exec.getExecutedAt());
    }

    @Test
    void shouldThrowWhenJobNameIsBlank() {
        assertThrows(IllegalArgumentException.class, () ->
                new JobExecution("  ", SCHEDULED, EXECUTED, JobExecution.Status.SUCCESS, null));
    }

    @Test
    void shouldThrowWhenJobNameIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                new JobExecution(null, SCHEDULED, EXECUTED, JobExecution.Status.SUCCESS, null));
    }

    @Test
    void shouldThrowWhenScheduledAtIsNull() {
        assertThrows(NullPointerException.class, () ->
                new JobExecution("my-job", null, EXECUTED, JobExecution.Status.SUCCESS, null));
    }

    @Test
    void shouldThrowWhenStatusIsNull() {
        assertThrows(NullPointerException.class, () ->
                new JobExecution("my-job", SCHEDULED, EXECUTED, null, null));
    }

    @Test
    void equalExecutionsShouldBeEqual() {
        JobExecution a = new JobExecution("job-x", SCHEDULED, EXECUTED, JobExecution.Status.SUCCESS, "ok");
        JobExecution b = new JobExecution("job-x", SCHEDULED, null,   JobExecution.Status.SUCCESS, "different message");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void differentStatusShouldNotBeEqual() {
        JobExecution a = new JobExecution("job-x", SCHEDULED, EXECUTED, JobExecution.Status.SUCCESS, null);
        JobExecution b = new JobExecution("job-x", SCHEDULED, null,   JobExecution.Status.MISSED,  null);
        assertNotEquals(a, b);
    }

    @Test
    void toStringShouldContainJobName() {
        JobExecution exec = new JobExecution("report-job", SCHEDULED, EXECUTED,
                JobExecution.Status.FAILURE, "Exit code 1");
        assertTrue(exec.toString().contains("report-job"));
        assertTrue(exec.toString().contains("FAILURE"));
    }
}
