package com.schedaudit.retry;

import com.schedaudit.alert.AlertService;
import com.schedaudit.model.JobExecution;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Manages retry policies per job and coordinates retry scheduling
 * with alerting when all retries are exhausted.
 */
public class RetryManager {

    private static final Logger logger = Logger.getLogger(RetryManager.class.getName());

    private final Map<String, RetryPolicy> policies = new ConcurrentHashMap<>();
    private final AlertService alertService;

    public RetryManager(AlertService alertService) {
        this.alertService = alertService;
    }

    public void registerPolicy(RetryPolicy policy) {
        policies.put(policy.getJobName(), policy);
        logger.info("Registered retry policy for job: " + policy.getJobName());
    }

    public Optional<RetryPolicy> getPolicy(String jobName) {
        return Optional.ofNullable(policies.get(jobName));
    }

    /**
     * Handles a failed or missed job execution. Returns true if a retry
     * should be scheduled, false if retries are exhausted.
     */
    public boolean handleFailure(JobExecution execution) {
        String jobName = execution.getJobName();
        RetryPolicy policy = policies.get(jobName);

        if (policy == null) {
            logger.warning("No retry policy found for job: " + jobName + ". Alerting immediately.");
            alertService.sendAlert(jobName, "No retry policy configured. Job failed at " + execution.getExecutionTime());
            return false;
        }

        if (policy.hasAttemptsRemaining()) {
            long delaySeconds = policy.nextDelay().getSeconds();
            logger.info(String.format("Scheduling retry %d/%d for job '%s' in %ds",
                    policy.getCurrentAttempt(), policy.getMaxAttempts(), jobName, delaySeconds));
            return true;
        } else {
            logger.severe("All retries exhausted for job: " + jobName);
            alertService.sendAlert(jobName,
                    "All " + policy.getMaxAttempts() + " retry attempts exhausted. Last failure: " + Instant.now());
            policy.reset();
            return false;
        }
    }

    public void resetPolicy(String jobName) {
        Optional.ofNullable(policies.get(jobName)).ifPresent(RetryPolicy::reset);
    }

    public int getPolicyCount() {
        return policies.size();
    }
}
