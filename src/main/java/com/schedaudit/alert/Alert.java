package com.schedaudit.alert;

import com.schedaudit.model.JobExecution;

import java.time.Instant;

/**
 * Immutable value object representing a single missed-run alert.
 */
public class Alert {

    private final String jobName;
    private final String cronExpression;
    private final String message;
    private final JobExecution lastKnownExecution;
    private final Instant issuedAt;

    public Alert(String jobName, String cronExpression, String message, JobExecution lastKnownExecution) {
        this.jobName = jobName;
        this.cronExpression = cronExpression;
        this.message = message;
        this.lastKnownExecution = lastKnownExecution;
        this.issuedAt = Instant.now();
    }

    public String getJobName() {
        return jobName;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public String getMessage() {
        return message;
    }

    public JobExecution getLastKnownExecution() {
        return lastKnownExecution;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    @Override
    public String toString() {
        return "Alert{" +
                "jobName='" + jobName + '\'' +
                ", cronExpression='" + cronExpression + '\'' +
                ", issuedAt=" + issuedAt +
                ", message='" + message + '\'' +
                '}';
    }
}
