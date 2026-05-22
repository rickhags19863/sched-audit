package com.schedaudit.retry;

import com.schedaudit.alert.AlertService;
import com.schedaudit.model.JobExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RetryManagerTest {

    private AlertService alertService;
    private RetryManager retryManager;

    @BeforeEach
    void setUp() {
        alertService = Mockito.mock(AlertService.class);
        retryManager = new RetryManager(alertService);
    }

    private JobExecution makeExecution(String jobName) {
        return new JobExecution(jobName, Instant.now(), false, "Test failure");
    }

    @Test
    void testRegisterAndRetrievePolicy() {
        RetryPolicy policy = new RetryPolicy("job-x", 3, Duration.ofSeconds(5), RetryPolicy.BackoffStrategy.FIXED);
        retryManager.registerPolicy(policy);
        Optional<RetryPolicy> retrieved = retryManager.getPolicy("job-x");
        assertTrue(retrieved.isPresent());
        assertEquals("job-x", retrieved.get().getJobName());
    }

    @Test
    void testHandleFailureReturnsTrueWhenAttemptsRemain() {
        RetryPolicy policy = new RetryPolicy("job-y", 3, Duration.ofSeconds(2), RetryPolicy.BackoffStrategy.FIXED);
        retryManager.registerPolicy(policy);
        boolean shouldRetry = retryManager.handleFailure(makeExecution("job-y"));
        assertTrue(shouldRetry);
        verifyNoInteractions(alertService);
    }

    @Test
    void testHandleFailureAlertsWhenAllAttemptsExhausted() {
        RetryPolicy policy = new RetryPolicy("job-z", 1, Duration.ofSeconds(1), RetryPolicy.BackoffStrategy.FIXED);
        retryManager.registerPolicy(policy);
        retryManager.handleFailure(makeExecution("job-z")); // uses attempt 1
        boolean shouldRetry = retryManager.handleFailure(makeExecution("job-z")); // exhausted
        assertFalse(shouldRetry);
        verify(alertService, times(1)).sendAlert(eq("job-z"), anyString());
    }

    @Test
    void testHandleFailureWithNoPolicyAlertsImmediately() {
        boolean shouldRetry = retryManager.handleFailure(makeExecution("unknown-job"));
        assertFalse(shouldRetry);
        verify(alertService, times(1)).sendAlert(eq("unknown-job"), anyString());
    }

    @Test
    void testResetPolicyAllowsRetryAgain() {
        RetryPolicy policy = new RetryPolicy("job-r", 1, Duration.ofSeconds(1), RetryPolicy.BackoffStrategy.FIXED);
        retryManager.registerPolicy(policy);
        retryManager.handleFailure(makeExecution("job-r")); // uses last attempt
        retryManager.resetPolicy("job-r");
        boolean shouldRetry = retryManager.handleFailure(makeExecution("job-r"));
        assertTrue(shouldRetry);
    }

    @Test
    void testGetPolicyCountReflectsRegistrations() {
        assertEquals(0, retryManager.getPolicyCount());
        retryManager.registerPolicy(new RetryPolicy("j1", 2, Duration.ofSeconds(1), RetryPolicy.BackoffStrategy.FIXED));
        retryManager.registerPolicy(new RetryPolicy("j2", 2, Duration.ofSeconds(1), RetryPolicy.BackoffStrategy.FIXED));
        assertEquals(2, retryManager.getPolicyCount());
    }
}
