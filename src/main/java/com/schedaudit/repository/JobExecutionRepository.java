package com.schedaudit.repository;

import com.schedaudit.model.JobExecution;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory repository for storing and querying JobExecution records.
 */
public class JobExecutionRepository {

    private final Map<String, List<JobExecution>> store = new ConcurrentHashMap<>();

    public void save(JobExecution execution) {
        if (execution == null || execution.getJobName() == null) {
            throw new IllegalArgumentException("JobExecution and jobName must not be null");
        }
        store.computeIfAbsent(execution.getJobName(), k -> new ArrayList<>())
             .add(execution);
    }

    public List<JobExecution> findByJobName(String jobName) {
        return store.getOrDefault(jobName, List.of());
    }

    public Optional<JobExecution> findLatestByJobName(String jobName) {
        return findByJobName(jobName).stream()
                .max((a, b) -> a.getStartTime().compareTo(b.getStartTime()));
    }

    public List<JobExecution> findByJobNameAndTimeRange(String jobName, Instant from, Instant to) {
        return findByJobName(jobName).stream()
                .filter(e -> !e.getStartTime().isBefore(from) && !e.getStartTime().isAfter(to))
                .collect(Collectors.toList());
    }

    public List<String> findAllJobNames() {
        return new ArrayList<>(store.keySet());
    }

    public int countByJobName(String jobName) {
        return findByJobName(jobName).size();
    }

    public void clear() {
        store.clear();
    }
}
