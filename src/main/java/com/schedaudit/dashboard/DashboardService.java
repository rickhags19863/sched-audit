package com.schedaudit.dashboard;

import com.schedaudit.model.JobExecution;
import com.schedaudit.repository.JobExecutionRepository;
import com.schedaudit.scheduler.ScheduleRegistry;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Aggregates execution history into summaries for dashboard consumption.
 */
public class DashboardService {

    private final JobExecutionRepository repository;
    private final ScheduleRegistry scheduleRegistry;

    public DashboardService(JobExecutionRepository repository, ScheduleRegistry scheduleRegistry) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
        this.scheduleRegistry = Objects.requireNonNull(scheduleRegistry, "scheduleRegistry must not be null");
    }

    /**
     * Returns a summary for every registered job.
     */
    public List<ExecutionSummary> getAllSummaries() {
        return scheduleRegistry.getRegisteredJobNames().stream()
                .map(this::getSummaryForJob)
                .collect(Collectors.toList());
    }

    /**
     * Builds an {@link ExecutionSummary} for the given job name.
     */
    public ExecutionSummary getSummaryForJob(String jobName) {
        Objects.requireNonNull(jobName, "jobName must not be null");
        List<JobExecution> executions = repository.findByJobName(jobName);

        long total = executions.size();
        Map<String, Long> countByStatus = executions.stream()
                .collect(Collectors.groupingBy(JobExecution::getStatus, Collectors.counting()));

        long successful = countByStatus.getOrDefault("SUCCESS", 0L);
        long failed = countByStatus.getOrDefault("FAILED", 0L);
        long missed = countByStatus.getOrDefault("MISSED", 0L);

        Instant lastExecution = executions.stream()
                .map(JobExecution::getExecutionTime)
                .filter(Objects::nonNull)
                .max(Instant::compareTo)
                .orElse(null);

        Instant nextExpected = scheduleRegistry.getNextExpectedTime(jobName);

        return new ExecutionSummary(jobName, total, successful, failed, missed, lastExecution, nextExpected);
    }
}
