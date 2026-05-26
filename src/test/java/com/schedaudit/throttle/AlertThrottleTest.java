package com.schedaudit.throttle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class AlertThrottleTest {

    private AlertThrottle throttle;

    @BeforeEach
    void setUp() {
        throttle = new AlertThrottle(Duration.ofMinutes(15));
    }

    @Test
    void shouldAllowFirstAlertForNewJob() {
        assertTrue(throttle.shouldAlert("job-alpha"));
    }

    @Test
    void shouldSuppressImmediateRepeatAlert() {
        throttle.shouldAlert("job-alpha");
        assertFalse(throttle.shouldAlert("job-alpha"));
    }

    @Test
    void shouldAllowAlertsForDifferentJobsIndependently() {
        assertTrue(throttle.shouldAlert("job-alpha"));
        assertTrue(throttle.shouldAlert("job-beta"));
    }

    @Test
    void shouldAllowAlertAfterReset() {
        throttle.shouldAlert("job-alpha");
        throttle.reset("job-alpha");
        assertTrue(throttle.shouldAlert("job-alpha"));
    }

    @Test
    void shouldAllowAllAlertsAfterResetAll() {
        throttle.shouldAlert("job-alpha");
        throttle.shouldAlert("job-beta");
        throttle.resetAll();
        assertTrue(throttle.shouldAlert("job-alpha"));
        assertTrue(throttle.shouldAlert("job-beta"));
    }

    @Test
    void shouldAllowAlertWithVeryShortCooldown() throws InterruptedException {
        AlertThrottle shortThrottle = new AlertThrottle(Duration.ofMillis(50));
        shortThrottle.shouldAlert("job-gamma");
        Thread.sleep(100);
        assertTrue(shortThrottle.shouldAlert("job-gamma"));
    }

    @Test
    void shouldThrowForNullJobName() {
        assertThrows(IllegalArgumentException.class, () -> throttle.shouldAlert(null));
    }

    @Test
    void shouldThrowForBlankJobName() {
        assertThrows(IllegalArgumentException.class, () -> throttle.shouldAlert("  "));
    }

    @Test
    void shouldThrowForNullCooldown() {
        assertThrows(IllegalArgumentException.class, () -> new AlertThrottle(null));
    }

    @Test
    void shouldThrowForZeroCooldown() {
        assertThrows(IllegalArgumentException.class, () -> new AlertThrottle(Duration.ZERO));
    }

    @Test
    void shouldReturnConfiguredCooldownPeriod() {
        Duration expected = Duration.ofMinutes(15);
        assertEquals(expected, throttle.getCooldownPeriod());
    }

    @Test
    void throttleConfigShouldUseDefaultCooldownWhenNoOverride() {
        ThrottleConfig config = new ThrottleConfig(Duration.ofMinutes(10));
        assertEquals(Duration.ofMinutes(10), config.getEffectiveCooldown("any-job"));
    }

    @Test
    void throttleConfigShouldUseJobOverrideWhenPresent() {
        ThrottleConfig config = new ThrottleConfig(Duration.ofMinutes(10));
        config.setJobCooldown("critical-job", Duration.ofHours(1));
        assertEquals(Duration.ofHours(1), config.getEffectiveCooldown("critical-job"));
        assertEquals(Duration.ofMinutes(10), config.getEffectiveCooldown("other-job"));
    }

    @Test
    void throttleConfigShouldDetectOverride() {
        ThrottleConfig config = new ThrottleConfig();
        assertFalse(config.hasOverride("job-x"));
        config.setJobCooldown("job-x", Duration.ofMinutes(5));
        assertTrue(config.hasOverride("job-x"));
    }
}
