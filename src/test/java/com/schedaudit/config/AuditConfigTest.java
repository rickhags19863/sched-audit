package com.schedaudit.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.Duration;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AuditConfig")
class AuditConfigTest {

    private AuditConfig configWith(String key, String value) {
        Properties p = new Properties();
        p.setProperty(key, value);
        return new AuditConfig(p);
    }

    @Test
    @DisplayName("returns defaults when no properties are set")
    void defaultValues() {
        AuditConfig config = new AuditConfig(new Properties());

        assertEquals(Duration.ofMinutes(10), config.getMissedRunThreshold());
        assertEquals(3, config.getMaxRetryAttempts());
        assertEquals(Duration.ofSeconds(30), config.getRetryBackoff());
        assertEquals("LOG", config.getNotificationChannel());
        assertTrue(config.isAlertEnabled());
        assertEquals("Sched-Audit Dashboard", config.getDashboardTitle());
        assertEquals(100, config.getExecutionHistoryLimit());
    }

    @Test
    @DisplayName("reads missed-run threshold from properties")
    void missedRunThreshold() {
        AuditConfig config = configWith("audit.missed-run.threshold-minutes", "20");
        assertEquals(Duration.ofMinutes(20), config.getMissedRunThreshold());
    }

    @Test
    @DisplayName("reads max retry attempts from properties")
    void maxRetryAttempts() {
        AuditConfig config = configWith("audit.retry.max-attempts", "5");
        assertEquals(5, config.getMaxRetryAttempts());
    }

    @Test
    @DisplayName("reads retry backoff from properties")
    void retryBackoff() {
        AuditConfig config = configWith("audit.retry.backoff-seconds", "60");
        assertEquals(Duration.ofSeconds(60), config.getRetryBackoff());
    }

    @Test
    @DisplayName("reads alert enabled flag from properties")
    void alertEnabled() {
        AuditConfig config = configWith("audit.alert.enabled", "false");
        assertFalse(config.isAlertEnabled());
    }

    @Test
    @DisplayName("reads notification channel from properties")
    void notificationChannel() {
        AuditConfig config = configWith("audit.notification.channel", "EMAIL");
        assertEquals("EMAIL", config.getNotificationChannel());
    }

    @Test
    @DisplayName("falls back to default for non-numeric long value")
    void invalidLongFallsBackToDefault() {
        AuditConfig config = configWith("audit.retry.max-attempts", "not-a-number");
        assertEquals(3, config.getMaxRetryAttempts());
    }

    @Test
    @DisplayName("reads execution history limit from properties")
    void executionHistoryLimit() {
        AuditConfig config = configWith("audit.history.limit", "250");
        assertEquals(250, config.getExecutionHistoryLimit());
    }
}
