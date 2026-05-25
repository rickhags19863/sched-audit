package com.schedaudit.metrics;

import com.schedaudit.model.JobExecution;
import com.schedaudit.repository.JobExecutionRepository;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

/**
 * Computes {@link JobMetrics} for registered jobs over a given time window.
 */
public class MetricsCollector {

    private final JobExecutionRepository repository;

    public MetricsCollector(JobExecutionRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
    }

    /**
     * Collects metrics for all executions of {@code jobName} within [windowStart, windowEnd].
     *
     * @param jobName     the job identifier
     * @param windowStart start of the observation window (inclusive)
     * @param windowEnd   end of the observation window (inclusive)
     * @return aggregated {@link JobMetrics}
     */
    public JobMetrics collect(String jobName, Instant windowStart, Instant windowEnd) {
        Objects.requireNonNull(jobName, "jobName must not be null");
        Objects.requireNonNull(windowStart);
        Objects.requireNonNull(windowEnd);
        if (windowEnd.isBefore(windowStart)) {
            throw new IllegalArgumentException("windowEnd must not be before windowStart");
        }

        List<JobExecution> executions = repository.findByJobNameAndTimeRange(jobName, windowStart, windowEnd);

        int total = executions.size();
        int successful = (int) executions.stream().filter(e -> "SUCCESS".equalsIgnoreCase(e.getStatus())).count();
        int failed    = (int) executions.stream().filter(e -> "FAILED".equalsIgnoreCase(e.getStatus())).count();
        int missed    = (int) executions.stream().filter(e -> "MISSED".equalsIgnoreCase(e.getStatus())).count();

        OptionalDouble avg = executions.stream()
                .filter(e -> e.getDurationMs() != null)
                .mapToLong(JobExecution::getDurationMs)
                .average();

        return new JobMetrics(jobName, total, successful, failed, missed,
                avg.orElse(0.0), windowStart, windowEnd);
    }

    /**
     * Collects metrics grouped by job name for all jobs within the window.
     */
    public Map<String, JobMetrics> collectAll(Instant windowStart, Instant windowEnd) {
        List<JobExecution> all = repository.findByTimeRange(windowStart, windowEnd);
        return all.stream()
                .collect(Collectors.groupingBy(JobExecution::getJobName))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> collect(e.getKey(), windowStart, windowEnd)
                ));
    }
}
