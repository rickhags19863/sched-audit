package com.schedaudit.audit;

import com.schedaudit.model.JobExecution;
import com.schedaudit.repository.JobExecutionRepository;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Logger;

/**
 * Logs job execution history and provides audit trail queries.
 */
public class AuditLogger {

    private static final Logger LOGGER = Logger.getLogger(AuditLogger.class.getName());
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    private final JobExecutionRepository repository;

    public AuditLogger(JobExecutionRepository repository) {
        if (repository == null) {
            throw new IllegalArgumentException("Repository must not be null");
        }
        this.repository = repository;
    }

    /**
     * Records a job execution event and logs it.
     *
     * @param execution the job execution to record
     */
    public void record(JobExecution execution) {
        if (execution == null) {
            throw new IllegalArgumentException("JobExecution must not be null");
        }
        repository.save(execution);
        LOGGER.info(String.format("[AUDIT] Job '%s' executed at %s — status: %s",
                execution.getJobName(),
                FORMATTER.format(execution.getExecutedAt()),
                execution.getStatus()));
    }

    /**
     * Retrieves and logs the full execution history for a given job.
     *
     * @param jobName the name of the job
     * @return list of executions ordered by time
     */
    public List<JobExecution> getHistory(String jobName) {
        if (jobName == null || jobName.isBlank()) {
            throw new IllegalArgumentException("Job name must not be blank");
        }
        List<JobExecution> history = repository.findByJobName(jobName);
        LOGGER.info(String.format("[AUDIT] Retrieved %d execution record(s) for job '%s'",
                history.size(), jobName));
        return history;
    }

    /**
     * Retrieves executions for a job within a time window.
     *
     * @param jobName the name of the job
     * @param from    start of the window (inclusive)
     * @param to      end of the window (inclusive)
     * @return filtered list of executions
     */
    public List<JobExecution> getHistoryBetween(String jobName, Instant from, Instant to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Time bounds must not be null");
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("'from' must not be after 'to'");
        }
        List<JobExecution> history = repository.findByJobNameAndTimeRange(jobName, from, to);
        LOGGER.info(String.format("[AUDIT] Retrieved %d execution record(s) for job '%s' between %s and %s",
                history.size(), jobName, FORMATTER.format(from), FORMATTER.format(to)));
        return history;
    }
}
