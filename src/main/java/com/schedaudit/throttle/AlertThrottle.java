package com.schedaudit.throttle;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Prevents alert flooding by suppressing duplicate alerts for the same job
 * within a configurable cooldown window.
 */
public class AlertThrottle {

    private final Duration cooldownPeriod;
    private final Map<String, Instant> lastAlertTime = new ConcurrentHashMap<>();

    public AlertThrottle(Duration cooldownPeriod) {
        if (cooldownPeriod == null || cooldownPeriod.isNegative() || cooldownPeriod.isZero()) {
            throw new IllegalArgumentException("Cooldown period must be a positive duration");
        }
        this.cooldownPeriod = cooldownPeriod;
    }

    /**
     * Returns true if an alert for the given job is allowed (i.e., not throttled).
     * Records the alert time if allowed.
     *
     * @param jobName the name of the job being alerted on
     * @return true if the alert should be sent, false if suppressed
     */
    public boolean shouldAlert(String jobName) {
        if (jobName == null || jobName.isBlank()) {
            throw new IllegalArgumentException("Job name must not be null or blank");
        }
        Instant now = Instant.now();
        Instant last = lastAlertTime.get(jobName);
        if (last == null || Duration.between(last, now).compareTo(cooldownPeriod) > 0) {
            lastAlertTime.put(jobName, now);
            return true;
        }
        return false;
    }

    /**
     * Clears the throttle state for a specific job.
     *
     * @param jobName the job to reset
     */
    public void reset(String jobName) {
        lastAlertTime.remove(jobName);
    }

    /**
     * Clears all throttle state.
     */
    public void resetAll() {
        lastAlertTime.clear();
    }

    public Duration getCooldownPeriod() {
        return cooldownPeriod;
    }

    public boolean isThrottled(String jobName) {
        return !shouldAllowWithoutRecording(jobName);
    }

    private boolean shouldAllowWithoutRecording(String jobName) {
        Instant last = lastAlertTime.get(jobName);
        if (last == null) return true;
        return Duration.between(last, Instant.now()).compareTo(cooldownPeriod) > 0;
    }
}
