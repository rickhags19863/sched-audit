package com.schedaudit.retry;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class RetryPolicyTest {

    @Test
    void testFixedBackoffReturnsConstantDelay() {
        RetryPolicy policy = new RetryPolicy("job-a", 3, Duration.ofSeconds(10), RetryPolicy.BackoffStrategy.FIXED);
        assertEquals(Duration.ofSeconds(10), policy.nextDelay());
        assertEquals(Duration.ofSeconds(10), policy.nextDelay());
        assertEquals(Duration.ofSeconds(10), policy.nextDelay());
        assertFalse(policy.hasAttemptsRemaining());
    }

    @Test
    void testExponentialBackoffDoublesDelay() {
        RetryPolicy policy = new RetryPolicy("job-b", 3, Duration.ofSeconds(5), RetryPolicy.BackoffStrategy.EXPONENTIAL);
        assertEquals(Duration.ofSeconds(5), policy.nextDelay());   // 5 * 2^0
        assertEquals(Duration.ofSeconds(10), policy.nextDelay());  // 5 * 2^1
        assertEquals(Duration.ofSeconds(20), policy.nextDelay());  // 5 * 2^2
    }

    @Test
    void testLinearBackoffIncrementsDelay() {
        RetryPolicy policy = new RetryPolicy("job-c", 3, Duration.ofSeconds(4), RetryPolicy.BackoffStrategy.LINEAR);
        assertEquals(Duration.ofSeconds(4), policy.nextDelay());   // 4 * 1
        assertEquals(Duration.ofSeconds(8), policy.nextDelay());   // 4 * 2
        assertEquals(Duration.ofSeconds(12), policy.nextDelay());  // 4 * 3
    }

    @Test
    void testHasAttemptsRemainingDecrementsCorrectly() {
        RetryPolicy policy = new RetryPolicy("job-d", 2, Duration.ofSeconds(1), RetryPolicy.BackoffStrategy.FIXED);
        assertTrue(policy.hasAttemptsRemaining());
        policy.nextDelay();
        assertTrue(policy.hasAttemptsRemaining());
        policy.nextDelay();
        assertFalse(policy.hasAttemptsRemaining());
    }

    @Test
    void testNextDelayThrowsWhenExhausted() {
        RetryPolicy policy = new RetryPolicy("job-e", 1, Duration.ofSeconds(1), RetryPolicy.BackoffStrategy.FIXED);
        policy.nextDelay();
        assertThrows(IllegalStateException.class, policy::nextDelay);
    }

    @Test
    void testResetRestoresAttemptCount() {
        RetryPolicy policy = new RetryPolicy("job-f", 2, Duration.ofSeconds(2), RetryPolicy.BackoffStrategy.FIXED);
        policy.nextDelay();
        policy.nextDelay();
        assertFalse(policy.hasAttemptsRemaining());
        policy.reset();
        assertTrue(policy.hasAttemptsRemaining());
        assertEquals(0, policy.getCurrentAttempt());
    }

    @Test
    void testInvalidConstructorArguments() {
        assertThrows(IllegalArgumentException.class, () ->
                new RetryPolicy("", 3, Duration.ofSeconds(1), RetryPolicy.BackoffStrategy.FIXED));
        assertThrows(IllegalArgumentException.class, () ->
                new RetryPolicy("job", 0, Duration.ofSeconds(1), RetryPolicy.BackoffStrategy.FIXED));
    }
}
