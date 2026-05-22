package com.schedaudit.retry;

import java.time.Duration;

/**
 * Defines the retry policy for a cron job that has missed its scheduled run.
 */
public class RetryPolicy {

    public enum BackoffStrategy {
        FIXED, EXPONENTIAL, LINEAR
    }

    private final String jobName;
    private final int maxAttempts;
    private final Duration initialDelay;
    private final BackoffStrategy backoffStrategy;
    private int currentAttempt;

    public RetryPolicy(String jobName, int maxAttempts, Duration initialDelay, BackoffStrategy backoffStrategy) {
        if (jobName == null || jobName.isBlank()) {
            throw new IllegalArgumentException("Job name must not be null or blank");
        }
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        this.jobName = jobName;
        this.maxAttempts = maxAttempts;
        this.initialDelay = initialDelay;
        this.backoffStrategy = backoffStrategy;
        this.currentAttempt = 0;
    }

    public boolean hasAttemptsRemaining() {
        return currentAttempt < maxAttempts;
    }

    public Duration nextDelay() {
        if (!hasAttemptsRemaining()) {
            throw new IllegalStateException("No retry attempts remaining for job: " + jobName);
        }
        Duration delay;
        switch (backoffStrategy) {
            case EXPONENTIAL:
                delay = initialDelay.multipliedBy((long) Math.pow(2, currentAttempt));
                break;
            case LINEAR:
                delay = initialDelay.multipliedBy(currentAttempt + 1);
                break;
            case FIXED:
            default:
                delay = initialDelay;
                break;
        }
        currentAttempt++;
        return delay;
    }

    public void reset() {
        this.currentAttempt = 0;
    }

    public String getJobName() { return jobName; }
    public int getMaxAttempts() { return maxAttempts; }
    public int getCurrentAttempt() { return currentAttempt; }
    public Duration getInitialDelay() { return initialDelay; }
    public BackoffStrategy getBackoffStrategy() { return backoffStrategy; }
}
