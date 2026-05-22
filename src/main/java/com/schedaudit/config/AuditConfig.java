package com.schedaudit.config;

import java.time.Duration;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Central configuration for sched-audit.
 * Loads settings from audit.properties on the classpath,
 * with sensible defaults when keys are absent.
 */
public class AuditConfig {

    private static final Logger LOGGER = Logger.getLogger(AuditConfig.class.getName());
    private static final String CONFIG_FILE = "audit.properties";

    private final Properties props;

    public AuditConfig() {
        props = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (in != null) {
                props.load(in);
                LOGGER.info("Loaded configuration from " + CONFIG_FILE);
            } else {
                LOGGER.warning(CONFIG_FILE + " not found – using defaults");
            }
        } catch (IOException e) {
            LOGGER.severe("Failed to load " + CONFIG_FILE + ": " + e.getMessage());
        }
    }

    /** Constructor for testing – accepts a pre-populated Properties object. */
    public AuditConfig(Properties props) {
        this.props = new Properties(props);
    }

    public Duration getMissedRunThreshold() {
        long minutes = getLong("audit.missed-run.threshold-minutes", 10L);
        return Duration.ofMinutes(minutes);
    }

    public int getMaxRetryAttempts() {
        return (int) getLong("audit.retry.max-attempts", 3L);
    }

    public Duration getRetryBackoff() {
        long seconds = getLong("audit.retry.backoff-seconds", 30L);
        return Duration.ofSeconds(seconds);
    }

    public String getNotificationChannel() {
        return props.getProperty("audit.notification.channel", "LOG");
    }

    public boolean isAlertEnabled() {
        return Boolean.parseBoolean(props.getProperty("audit.alert.enabled", "true"));
    }

    public String getDashboardTitle() {
        return props.getProperty("audit.dashboard.title", "Sched-Audit Dashboard");
    }

    public int getExecutionHistoryLimit() {
        return (int) getLong("audit.history.limit", 100L);
    }

    private long getLong(String key, long defaultValue) {
        String val = props.getProperty(key);
        if (val == null) return defaultValue;
        try {
            return Long.parseLong(val.trim());
        } catch (NumberFormatException e) {
            LOGGER.warning("Invalid value for " + key + "='" + val + "', using default " + defaultValue);
            return defaultValue;
        }
    }
}
