package com.schedaudit.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ScheduleRegistryTest {

    private ScheduleRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new ScheduleRegistry();
    }

    @Test
    void register_andRetrieve_succeeds() {
        registry.register("billing-job", "*/15 * * * *");
        Optional<CronScheduleParser> parser = registry.getSchedule("billing-job");
        assertTrue(parser.isPresent());
        assertEquals("*/15 * * * *", parser.get().getExpression());
    }

    @Test
    void register_blankJobName_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> registry.register("  ", "*/5 * * * *"));
    }

    @Test
    void register_invalidCronExpression_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> registry.register("bad-job", "not-a-cron"));
    }

    @Test
    void deregister_existingJob_returnsTrue() {
        registry.register("cleanup-job", "*/30 * * * *");
        assertTrue(registry.deregister("cleanup-job"));
        assertFalse(registry.isRegistered("cleanup-job"));
    }

    @Test
    void deregister_unknownJob_returnsFalse() {
        assertFalse(registry.deregister("ghost-job"));
    }

    @Test
    void registeredJobs_reflectsCurrentState() {
        registry.register("job-a", "*/5 * * * *");
        registry.register("job-b", "*/10 * * * *");
        assertTrue(registry.registeredJobs().contains("job-a"));
        assertTrue(registry.registeredJobs().contains("job-b"));
        assertEquals(2, registry.registeredJobs().size());
    }

    @Test
    void cronParser_intervalMinutes_correctForEveryFiveMinutes() {
        registry.register("sync-job", "*/5 * * * *");
        CronScheduleParser parser = registry.getSchedule("sync-job").orElseThrow();
        assertEquals(5L, parser.getIntervalMinutes());
    }

    @Test
    void cronParser_lastExpectedRunBefore_alignsToInterval() {
        registry.register("report-job", "*/15 * * * *");
        CronScheduleParser parser = registry.getSchedule("report-job").orElseThrow();
        LocalDateTime ref = LocalDateTime.of(2024, 6, 1, 10, 22);
        LocalDateTime last = parser.lastExpectedRunBefore(ref);
        assertEquals(0, last.getMinute() % 15,
                "Last expected run minute should be divisible by 15");
        assertTrue(!last.isAfter(ref));
    }

    @Test
    void cronParser_nextExpectedRunAfter_isAfterReference() {
        registry.register("heartbeat-job", "*/10 * * * *");
        CronScheduleParser parser = registry.getSchedule("heartbeat-job").orElseThrow();
        LocalDateTime ref = LocalDateTime.of(2024, 6, 1, 9, 5);
        LocalDateTime next = parser.nextExpectedRunAfter(ref);
        assertTrue(next.isAfter(ref));
    }
}
