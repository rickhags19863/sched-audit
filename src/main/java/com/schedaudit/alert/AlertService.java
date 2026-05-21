package com.schedaudit.alert;

import com.schedaudit.model.JobExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Service responsible for issuing alerts when missed job runs are detected.
 * Alerts are logged and stored in-memory for retrieval via the API.
 */
@Service
public class AlertService {

    private static final Logger log = LoggerFactory.getLogger(AlertService.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final List<Alert> issuedAlerts = new ArrayList<>();

    /**
     * Issues an alert for a job that missed its expected execution window.
     *
     * @param jobName       the name of the job that missed its run
     * @param missedExecution the last known execution record (may be null if never run)
     * @param expectedCron  the cron expression defining the schedule
     */
    public void alertMissedRun(String jobName, JobExecution missedExecution, String expectedCron) {
        String lastRunInfo = missedExecution != null
                ? "last run at " + missedExecution.getStartTime().format(FORMATTER)
                : "no previous run recorded";

        String message = String.format(
                "MISSED RUN ALERT: Job '%s' (cron: %s) has not executed on schedule. %s.",
                jobName, expectedCron, lastRunInfo
        );

        log.warn(message);

        Alert alert = new Alert(jobName, expectedCron, message, missedExecution);
        issuedAlerts.add(alert);
    }

    /**
     * Returns an unmodifiable view of all alerts issued during this session.
     */
    public List<Alert> getIssuedAlerts() {
        return Collections.unmodifiableList(issuedAlerts);
    }

    /**
     * Clears all stored alerts (useful for testing or scheduled cleanup).
     */
    public void clearAlerts() {
        issuedAlerts.clear();
    }
}
