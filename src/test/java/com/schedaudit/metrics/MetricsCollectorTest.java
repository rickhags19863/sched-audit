package com.schedaudit.metrics;

import com.schedaudit.model.JobExecution;
import com.schedaudit.repository.JobExecutionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetricsCollectorTest {

    @Mock
    private JobExecutionRepository repository;

    private MetricsCollector collector;

    private final Instant start = Instant.parse("2024-01-01T00:00:00Z");
    private final Instant end   = Instant.parse("2024-01-02T00:00:00Z");

    @BeforeEach
    void setUp() {
        collector = new MetricsCollector(repository);
    }

    @Test
    void collect_returnsCorrectCounts() {
        List<JobExecution> executions = Arrays.asList(
                execution("backup", "SUCCESS", 500L),
                execution("backup", "SUCCESS", 600L),
                execution("backup", "FAILED",  null),
                execution("backup", "MISSED",  null)
        );
        when(repository.findByJobNameAndTimeRange("backup", start, end)).thenReturn(executions);

        JobMetrics metrics = collector.collect("backup", start, end);

        assertThat(metrics.getTotalRuns()).isEqualTo(4);
        assertThat(metrics.getSuccessfulRuns()).isEqualTo(2);
        assertThat(metrics.getFailedRuns()).isEqualTo(1);
        assertThat(metrics.getMissedRuns()).isEqualTo(1);
        assertThat(metrics.getAvgDurationMs()).isEqualTo(550.0);
        assertThat(metrics.successRate()).isEqualTo(0.5);
    }

    @Test
    void collect_emptyList_returnsZeroMetrics() {
        when(repository.findByJobNameAndTimeRange("noop", start, end)).thenReturn(Collections.emptyList());

        JobMetrics metrics = collector.collect("noop", start, end);

        assertThat(metrics.getTotalRuns()).isZero();
        assertThat(metrics.successRate()).isZero();
        assertThat(metrics.getAvgDurationMs()).isZero();
    }

    @Test
    void collect_throwsWhenWindowEndBeforeStart() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> collector.collect("job", end, start));
    }

    @Test
    void collectAll_groupsByJobName() {
        List<JobExecution> all = Arrays.asList(
                execution("jobA", "SUCCESS", 100L),
                execution("jobB", "FAILED",  200L)
        );
        when(repository.findByTimeRange(start, end)).thenReturn(all);
        when(repository.findByJobNameAndTimeRange(eq("jobA"), any(), any()))
                .thenReturn(Collections.singletonList(execution("jobA", "SUCCESS", 100L)));
        when(repository.findByJobNameAndTimeRange(eq("jobB"), any(), any()))
                .thenReturn(Collections.singletonList(execution("jobB", "FAILED", 200L)));

        Map<String, JobMetrics> result = collector.collectAll(start, end);

        assertThat(result).containsKeys("jobA", "jobB");
        assertThat(result.get("jobA").getSuccessfulRuns()).isEqualTo(1);
        assertThat(result.get("jobB").getFailedRuns()).isEqualTo(1);
    }

    private JobExecution execution(String jobName, String status, Long durationMs) {
        JobExecution e = new JobExecution();
        e.setJobName(jobName);
        e.setStatus(status);
        e.setDurationMs(durationMs);
        return e;
    }
}
