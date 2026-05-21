package com.schedaudit.model;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Represents a single execution record for a scheduled cron job.
 */
public class JobExecution {

    public enum Status {
        SUCCESS,
        FAILURE,
        MISSED
    }

    private final String jobName;
    private final Instant scheduledAt;
    private final Instant executedAt;
    private final Status status;
    private final String message;

    public JobExecution(String jobName, Instant scheduledAt, Instant executedAt, Status status, String message) {
        if (jobName == null || jobName.isBlank()) {
            throw new IllegalArgumentException("jobName must not be null or blank");
        }
        Objects.requireNonNull(scheduledAt, "scheduledAt must not be null");
        Objects.requireNonNull(status, "status must not be null");
        this.jobName = jobName;
        this.scheduledAt = scheduledAt;
        this.executedAt = executedAt;
        this.status = status;
        this.message = message;
    }

    public String getJobName() { return jobName; }
    public Instant getScheduledAt() { return scheduledAt; }
    public Instant getExecutedAt() { return executedAt; }
    public Status getStatus() { return status; }
    public String getMessage() { return message; }

    public boolean isMissed() {
        return status == Status.MISSED;
    }

    /**
     * Returns the delay between the scheduled time and the actual execution time.
     * A positive duration indicates the job ran late; negative indicates it ran early.
     *
     * @return the delay as a {@link Duration}, or {@code null} if {@code executedAt} is not set
     */
    public Duration getExecutionDelay() {
        if (executedAt == null) {
            return null;
        }
        return Duration.between(scheduledAt, executedAt);
    }

    @Override
    public String toString() {
        return String.format("JobExecution{jobName='%s', scheduledAt=%s, executedAt=%s, status=%s, message='%s'}",
                jobName, scheduledAt, executedAt, status, message);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JobExecution)) return false;
        JobExecution that = (JobExecution) o;
        return Objects.equals(jobName, that.jobName) &&
               Objects.equals(scheduledAt, that.scheduledAt) &&
               status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobName, scheduledAt, status);
    }
}
