package com.schedaudit.history;

import com.schedaudit.model.JobExecution;
import com.schedaudit.repository.JobExecutionRepository;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for querying and summarizing job execution history.
 */
public class ExecutionHistoryService {

    private final JobExecutionRepository repository;

    public ExecutionHistoryService(JobExecutionRepository repository) {
        if (repository == null) {
            throw new IllegalArgumentException("Repository must not be null");
        }
        this.repository = repository;
    }

    /**
     * Returns all executions for a given job within the specified time range.
     */
    public List<JobExecution> getHistory(String jobName, Instant from, Instant to) {
        if (jobName == null || jobName.isBlank()) {
            throw new IllegalArgumentException("Job name must not be blank");
        }
        if (from == null || to == null) {
            throw new IllegalArgumentException("Time range bounds must not be null");
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("'from' must not be after 'to'");
        }
        return repository.findByJobName(jobName).stream()
                .filter(e -> !e.getStartTime().isBefore(from) && !e.getStartTime().isAfter(to))
                .sorted((a, b) -> a.getStartTime().compareTo(b.getStartTime()))
                .collect(Collectors.toList());
    }

    /**
     * Returns the most recent execution for each distinct job name.
     */
    public Map<String, JobExecution> getLatestPerJob() {
        return repository.findAll().stream()
                .collect(Collectors.toMap(
                        JobExecution::getJobName,
                        e -> e,
                        (existing, candidate) ->
                                candidate.getStartTime().isAfter(existing.getStartTime())
                                        ? candidate : existing
                ));
    }

    /**
     * Returns the count of executions grouped by status for a given job.
     */
    public Map<String, Long> getStatusBreakdown(String jobName) {
        if (jobName == null || jobName.isBlank()) {
            throw new IllegalArgumentException("Job name must not be blank");
        }
        return repository.findByJobName(jobName).stream()
                .collect(Collectors.groupingBy(
                        e -> e.getStatus() != null ? e.getStatus() : "UNKNOWN",
                        Collectors.counting()
                ));
    }

    /**
     * Returns all distinct job names tracked in the repository.
     */
    public List<String> getTrackedJobNames() {
        return repository.findAll().stream()
                .map(JobExecution::getJobName)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
}
