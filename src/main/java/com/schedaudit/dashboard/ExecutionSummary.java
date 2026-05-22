package com.schedaudit.dashboard;

import java.time.Instant;
import java.util.Objects;

/**
 * Immutable summary of a job's execution statistics for dashboard display.
 */
public class ExecutionSummary {

    private final String jobName;
    private final long totalRuns;
    private final long successfulRuns;
    private final long failedRuns;
    private final long missedRuns;
    private final Instant lastExecutionTime;
    private final Instant nextExpectedTime;

    public ExecutionSummary(String jobName, long totalRuns, long successfulRuns,
                            long failedRuns, long missedRuns,
                            Instant lastExecutionTime, Instant nextExpectedTime) {
        this.jobName = Objects.requireNonNull(jobName, "jobName must not be null");
        this.totalRuns = totalRuns;
        this.successfulRuns = successfulRuns;
        this.failedRuns = failedRuns;
        this.missedRuns = missedRuns;
        this.lastExecutionTime = lastExecutionTime;
        this.nextExpectedTime = nextExpectedTime;
    }

    public String getJobName() { return jobName; }
    public long getTotalRuns() { return totalRuns; }
    public long getSuccessfulRuns() { return successfulRuns; }
    public long getFailedRuns() { return failedRuns; }
    public long getMissedRuns() { return missedRuns; }
    public Instant getLastExecutionTime() { return lastExecutionTime; }
    public Instant getNextExpectedTime() { return nextExpectedTime; }

    public double getSuccessRate() {
        if (totalRuns == 0) return 0.0;
        return (double) successfulRuns / totalRuns * 100.0;
    }

    @Override
    public String toString() {
        return String.format("ExecutionSummary{job='%s', total=%d, success=%d, failed=%d, missed=%d, successRate=%.1f%%}",
                jobName, totalRuns, successfulRuns, failedRuns, missedRuns, getSuccessRate());
    }
}
