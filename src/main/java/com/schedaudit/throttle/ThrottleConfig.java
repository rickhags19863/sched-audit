package com.schedaudit.throttle;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for alert throttling. Supports a global default cooldown
 * and per-job overrides.
 */
public class ThrottleConfig {

    private static final Duration DEFAULT_COOLDOWN = Duration.ofMinutes(30);

    private Duration defaultCooldown;
    private final Map<String, Duration> jobOverrides = new HashMap<>();

    public ThrottleConfig() {
        this.defaultCooldown = DEFAULT_COOLDOWN;
    }

    public ThrottleConfig(Duration defaultCooldown) {
        if (defaultCooldown == null || defaultCooldown.isNegative() || defaultCooldown.isZero()) {
            throw new IllegalArgumentException("Default cooldown must be a positive duration");
        }
        this.defaultCooldown = defaultCooldown;
    }

    /**
     * Sets a per-job cooldown override.
     *
     * @param jobName  the job name
     * @param cooldown the cooldown duration for this job
     */
    public void setJobCooldown(String jobName, Duration cooldown) {
        if (jobName == null || jobName.isBlank()) {
            throw new IllegalArgumentException("Job name must not be null or blank");
        }
        if (cooldown == null || cooldown.isNegative() || cooldown.isZero()) {
            throw new IllegalArgumentException("Cooldown must be a positive duration");
        }
        jobOverrides.put(jobName, cooldown);
    }

    /**
     * Returns the effective cooldown for a given job.
     * Falls back to the default if no override is configured.
     *
     * @param jobName the job name
     * @return the effective cooldown duration
     */
    public Duration getEffectiveCooldown(String jobName) {
        return jobOverrides.getOrDefault(jobName, defaultCooldown);
    }

    public Duration getDefaultCooldown() {
        return defaultCooldown;
    }

    public void setDefaultCooldown(Duration defaultCooldown) {
        if (defaultCooldown == null || defaultCooldown.isNegative() || defaultCooldown.isZero()) {
            throw new IllegalArgumentException("Default cooldown must be a positive duration");
        }
        this.defaultCooldown = defaultCooldown;
    }

    public boolean hasOverride(String jobName) {
        return jobOverrides.containsKey(jobName);
    }

    public Map<String, Duration> getJobOverrides() {
        return Map.copyOf(jobOverrides);
    }
}
