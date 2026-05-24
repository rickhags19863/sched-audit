package com.schedaudit.filter;

import com.schedaudit.model.JobExecution;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides filtering capabilities over collections of JobExecution records.
 * Supports filtering by job name, status, and time range.
 */
public class ExecutionFilter {

    public enum Status {
        SUCCESS, FAILURE, MISSED, ANY
    }

    private String jobName;
    private Status status = Status.ANY;
    private Instant from;
    private Instant to;

    public ExecutionFilter withJobName(String jobName) {
        this.jobName = jobName;
        return this;
    }

    public ExecutionFilter withStatus(Status status) {
        this.status = status;
        return this;
    }

    public ExecutionFilter withFrom(Instant from) {
        this.from = from;
        return this;
    }

    public ExecutionFilter withTo(Instant to) {
        this.to = to;
        return this;
    }

    /**
     * Applies this filter to the given list of executions and returns matching records.
     *
     * @param executions the full list of job executions
     * @return filtered list
     */
    public List<JobExecution> apply(List<JobExecution> executions) {
        if (executions == null) {
            return List.of();
        }
        return executions.stream()
                .filter(e -> jobName == null || jobName.equals(e.getJobName()))
                .filter(e -> status == Status.ANY || status.name().equalsIgnoreCase(e.getStatus()))
                .filter(e -> from == null || !e.getExecutionTime().isBefore(from))
                .filter(e -> to == null || !e.getExecutionTime().isAfter(to))
                .collect(Collectors.toList());
    }

    public String getJobName() { return jobName; }
    public Status getStatus() { return status; }
    public Instant getFrom() { return from; }
    public Instant getTo() { return to; }
}
