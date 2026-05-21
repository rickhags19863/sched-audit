package com.schedaudit.scheduler;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory registry that maps job names to their cron schedule parsers.
 * Acts as the source of truth for expected job schedules.
 */
public class ScheduleRegistry {

    private final Map<String, CronScheduleParser> schedules = new ConcurrentHashMap<>();

    /**
     * Registers a job with its cron expression. Overwrites any existing entry.
     *
     * @param jobName    unique identifier for the job
     * @param cronExpr   standard 5-field cron expression
     */
    public void register(String jobName, String cronExpr) {
        if (jobName == null || jobName.isBlank()) {
            throw new IllegalArgumentException("Job name must not be blank");
        }
        schedules.put(jobName, new CronScheduleParser(cronExpr));
    }

    /**
     * Removes a job from the registry.
     *
     * @param jobName the job to deregister
     * @return true if the job was present and removed
     */
    public boolean deregister(String jobName) {
        return schedules.remove(jobName) != null;
    }

    /**
     * Looks up the schedule parser for a given job.
     *
     * @param jobName the job to look up
     * @return an Optional containing the parser, or empty if not registered
     */
    public Optional<CronScheduleParser> getSchedule(String jobName) {
        return Optional.ofNullable(schedules.get(jobName));
    }

    /**
     * Returns an unmodifiable view of all registered job names.
     */
    public java.util.Set<String> registeredJobs() {
        return Collections.unmodifiableSet(schedules.keySet());
    }

    /**
     * Returns true if the given job is currently registered.
     */
    public boolean isRegistered(String jobName) {
        return schedules.containsKey(jobName);
    }
}
