package com.schedaudit.service;

import com.schedaudit.model.JobExecution;
import com.schedaudit.repository.JobExecutionRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Detects missed cron job runs by comparing the last known execution
 * time against the expected schedule interval.
 */
public class MissedRunDetector {

    private static final Logger logger = Logger.getLogger(MissedRunDetector.class.getName());

    private final JobExecutionRepository repository;

    public MissedRunDetector(JobExecutionRepository repository) {
        this.repository = repository;
    }

    /**
     * Checks whether a job has missed its expected run.
     *
     * @param jobName          the name of the job to check
     * @param expectedInterval the interval at which the job should run
     * @param referenceTime    the point in time to check against (usually now)
     * @return true if the job appears to have missed a run
     */
    public boolean hasMissedRun(String jobName, Duration expectedInterval, Instant referenceTime) {
        Optional<JobExecution> latest = repository.findLatestByJobName(jobName);
        if (latest.isEmpty()) {
            logger.warning("No execution history found for job: " + jobName);
            return true;
        }
        Instant lastRun = latest.get().getStartTime();
        Duration elapsed = Duration.between(lastRun, referenceTime);
        boolean missed = elapsed.compareTo(expectedInterval) > 0;
        if (missed) {
            logger.warning(String.format(
                "Missed run detected for job '%s': last ran %s ago, expected every %s",
                jobName, elapsed, expectedInterval));
        }
        return missed;
    }

    /**
     * Returns a list of job names that have missed their expected run.
     *
     * @param jobIntervals map of job name to expected interval
     * @param referenceTime the point in time to check against
     * @return list of job names with missed runs
     */
    public List<String> detectMissedJobs(java.util.Map<String, Duration> jobIntervals, Instant referenceTime) {
        List<String> missed = new ArrayList<>();
        for (Map.Entry<String, Duration> entry : jobIntervals.entrySet()) {
            if (hasMissedRun(entry.getKey(), entry.getValue(), referenceTime)) {
                missed.add(entry.getKey());
            }
        }
        return missed;
    }
}
