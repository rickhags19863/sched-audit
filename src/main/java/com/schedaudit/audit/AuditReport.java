package com.schedaudit.audit;

import com.schedaudit.model.JobExecution;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Immutable summary report of job execution history.
 */
public class AuditReport {

    private final String jobName;
    private final Instant generatedAt;
    private final List<JobExecution> executions;
    private final Map<String, Long> statusCounts;

    public AuditReport(String jobName, List<JobExecution> executions) {
        if (jobName == null || jobName.isBlank()) {
            throw new IllegalArgumentException("Job name must not be blank");
        }
        if (executions == null) {
            throw new IllegalArgumentException("Executions list must not be null");
        }
        this.jobName = jobName;
        this.generatedAt = Instant.now();
        this.executions = Collections.unmodifiableList(executions);
        this.statusCounts = executions.stream()
                .collect(Collectors.groupingBy(JobExecution::getStatus, Collectors.counting()));
    }

    public String getJobName() {
        return jobName;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }

    public List<JobExecution> getExecutions() {
        return executions;
    }

    public int getTotalRuns() {
        return executions.size();
    }

    public long getCountByStatus(String status) {
        return statusCounts.getOrDefault(status, 0L);
    }

    public Map<String, Long> getStatusCounts() {
        return Collections.unmodifiableMap(statusCounts);
    }

    @Override
    public String toString() {
        return String.format("AuditReport{job='%s', totalRuns=%d, statuses=%s, generatedAt=%s}",
                jobName, getTotalRuns(), statusCounts, generatedAt);
    }
}
