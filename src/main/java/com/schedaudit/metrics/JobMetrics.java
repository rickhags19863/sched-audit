package com.schedaudit.metrics;

import java.time.Instant;
import java.util.Objects;

/**
 * Holds aggregated metrics for a single job over a time window.
 */
public class JobMetrics {

    private final String jobName;
    private final int totalRuns;
    private final int successfulRuns;
    private final int failedRuns;
    private final int missedRuns;
    private final double avgDurationMs;
    private final Instant windowStart;
    private final Instant windowEnd;

    public JobMetrics(String jobName, int totalRuns, int successfulRuns,
                      int failedRuns, int missedRuns, double avgDurationMs,
                      Instant windowStart, Instant windowEnd) {
        this.jobName = Objects.requireNonNull(jobName, "jobName must not be null");
        this.totalRuns = totalRuns;
        this.successfulRuns = successfulRuns;
        this.failedRuns = failedRuns;
        this.missedRuns = missedRuns;
        this.avgDurationMs = avgDurationMs;
        this.windowStart = Objects.requireNonNull(windowStart);
        this.windowEnd = Objects.requireNonNull(windowEnd);
    }

    public String getJobName() { return jobName; }
    public int getTotalRuns() { return totalRuns; }
    public int getSuccessfulRuns() { return successfulRuns; }
    public int getFailedRuns() { return failedRuns; }
    public int getMissedRuns() { return missedRuns; }
    public double getAvgDurationMs() { return avgDurationMs; }
    public Instant getWindowStart() { return windowStart; }
    public Instant getWindowEnd() { return windowEnd; }

    /** Success rate as a value between 0.0 and 1.0. */
    public double successRate() {
        if (totalRuns == 0) return 0.0;
        return (double) successfulRuns / totalRuns;
    }

    @Override
    public String toString() {
        return String.format("JobMetrics{job='%s', total=%d, success=%d, failed=%d, missed=%d, avgMs=%.2f}",
                jobName, totalRuns, successfulRuns, failedRuns, missedRuns, avgDurationMs);
    }
}
