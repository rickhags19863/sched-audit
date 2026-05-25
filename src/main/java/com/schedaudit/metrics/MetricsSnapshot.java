package com.schedaudit.metrics;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Immutable snapshot of metrics for multiple jobs captured at a point in time.
 */
public class MetricsSnapshot {

    private final Instant capturedAt;
    private final List<JobMetrics> jobMetricsList;

    public MetricsSnapshot(Instant capturedAt, List<JobMetrics> jobMetricsList) {
        this.capturedAt = Objects.requireNonNull(capturedAt, "capturedAt must not be null");
        this.jobMetricsList = Collections.unmodifiableList(
                Objects.requireNonNull(jobMetricsList, "jobMetricsList must not be null"));
    }

    public Instant getCapturedAt() {
        return capturedAt;
    }

    public List<JobMetrics> getJobMetricsList() {
        return jobMetricsList;
    }

    /** Returns overall success rate across all jobs in this snapshot. */
    public double overallSuccessRate() {
        int totalRuns = jobMetricsList.stream().mapToInt(JobMetrics::getTotalRuns).sum();
        int totalSuccess = jobMetricsList.stream().mapToInt(JobMetrics::getSuccessfulRuns).sum();
        if (totalRuns == 0) return 0.0;
        return (double) totalSuccess / totalRuns;
    }

    /** Returns total number of missed runs across all jobs. */
    public int totalMissedRuns() {
        return jobMetricsList.stream().mapToInt(JobMetrics::getMissedRuns).sum();
    }

    @Override
    public String toString() {
        return String.format("MetricsSnapshot{capturedAt=%s, jobs=%d, overallSuccessRate=%.2f}",
                capturedAt, jobMetricsList.size(), overallSuccessRate());
    }
}
