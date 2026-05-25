package com.schedaudit.metrics;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.*;

class MetricsSnapshotTest {

    private final Instant now   = Instant.now();
    private final Instant start = Instant.parse("2024-01-01T00:00:00Z");
    private final Instant end   = Instant.parse("2024-01-02T00:00:00Z");

    @Test
    void overallSuccessRate_calculatesAcrossAllJobs() {
        JobMetrics m1 = new JobMetrics("jobA", 4, 3, 1, 0, 100.0, start, end);
        JobMetrics m2 = new JobMetrics("jobB", 2, 1, 1, 0, 200.0, start, end);

        MetricsSnapshot snapshot = new MetricsSnapshot(now, Arrays.asList(m1, m2));

        // 4 successes out of 6 total
        assertThat(snapshot.overallSuccessRate()).isEqualTo(4.0 / 6.0);
    }

    @Test
    void totalMissedRuns_sumsAcrossJobs() {
        JobMetrics m1 = new JobMetrics("jobA", 5, 3, 1, 1, 0.0, start, end);
        JobMetrics m2 = new JobMetrics("jobB", 3, 2, 0, 1, 0.0, start, end);

        MetricsSnapshot snapshot = new MetricsSnapshot(now, Arrays.asList(m1, m2));

        assertThat(snapshot.totalMissedRuns()).isEqualTo(2);
    }

    @Test
    void overallSuccessRate_noRuns_returnsZero() {
        MetricsSnapshot snapshot = new MetricsSnapshot(now, Collections.emptyList());
        assertThat(snapshot.overallSuccessRate()).isZero();
    }

    @Test
    void jobMetricsList_isImmutable() {
        MetricsSnapshot snapshot = new MetricsSnapshot(now, Collections.emptyList());
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> snapshot.getJobMetricsList().add(
                        new JobMetrics("x", 0, 0, 0, 0, 0.0, start, end)));
    }

    @Test
    void toString_containsKeyInfo() {
        JobMetrics m = new JobMetrics("jobA", 2, 2, 0, 0, 50.0, start, end);
        MetricsSnapshot snapshot = new MetricsSnapshot(now, Collections.singletonList(m));
        assertThat(snapshot.toString()).contains("MetricsSnapshot").contains("jobs=1");
    }
}
