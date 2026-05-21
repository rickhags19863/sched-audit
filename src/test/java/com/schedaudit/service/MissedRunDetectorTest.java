package com.schedaudit.service;

import com.schedaudit.model.JobExecution;
import com.schedaudit.repository.JobExecutionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MissedRunDetectorTest {

    private JobExecutionRepository repository;
    private MissedRunDetector detector;

    @BeforeEach
    void setUp() {
        repository = new JobExecutionRepository();
        detector = new MissedRunDetector(repository);
    }

    @Test
    void hasMissedRun_returnsTrueWhenNoHistory() {
        assertTrue(detector.hasMissedRun("daily-report", Duration.ofHours(24), Instant.now()));
    }

    @Test
    void hasMissedRun_returnsFalseWhenRecentExecution() {
        Instant now = Instant.now();
        JobExecution exec = new JobExecution("hourly-sync", now.minus(Duration.ofMinutes(30)), null, true, null);
        repository.save(exec);

        assertFalse(detector.hasMissedRun("hourly-sync", Duration.ofHours(1), now));
    }

    @Test
    void hasMissedRun_returnsTrueWhenOverdue() {
        Instant now = Instant.now();
        JobExecution exec = new JobExecution("hourly-sync", now.minus(Duration.ofHours(2)), null, true, null);
        repository.save(exec);

        assertTrue(detector.hasMissedRun("hourly-sync", Duration.ofHours(1), now));
    }

    @Test
    void hasMissedRun_returnsFalseWhenExecutionIsExactlyOnInterval() {
        Instant now = Instant.now();
        // Execution at exactly the interval boundary should not be considered missed
        JobExecution exec = new JobExecution("hourly-sync", now.minus(Duration.ofHours(1)), null, true, null);
        repository.save(exec);

        assertFalse(detector.hasMissedRun("hourly-sync", Duration.ofHours(1), now));
    }

    @Test
    void detectMissedJobs_returnsOnlyMissedJobs() {
        Instant now = Instant.now();

        JobExecution recentExec = new JobExecution("job-ok", now.minus(Duration.ofMinutes(10)), null, true, null);
        repository.save(recentExec);

        JobExecution overdueExec = new JobExecution("job-late", now.minus(Duration.ofHours(3)), null, true, null);
        repository.save(overdueExec);

        Map<String, Duration> intervals = Map.of(
            "job-ok",   Duration.ofHours(1),
            "job-late", Duration.ofHours(1),
            "job-new",  Duration.ofHours(1)
        );

        List<String> missed = detector.detectMissedJobs(intervals, now);

        assertEquals(2, missed.size());
        assertTrue(missed.contains("job-late"));
        assertTrue(missed.contains("job-new"));
        assertFalse(missed.contains("job-ok"));
    }
}
