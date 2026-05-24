package com.schedaudit.history;

import java.time.Instant;
import java.util.Objects;

/**
 * Immutable value object encapsulating parameters for an execution history query.
 */
public final class HistoryQuery {

    private final String jobName;
    private final Instant from;
    private final Instant to;
    private final int maxResults;

    private static final int DEFAULT_MAX_RESULTS = 100;

    private HistoryQuery(Builder builder) {
        this.jobName = builder.jobName;
        this.from = builder.from;
        this.to = builder.to;
        this.maxResults = builder.maxResults;
    }

    public String getJobName() { return jobName; }
    public Instant getFrom() { return from; }
    public Instant getTo() { return to; }
    public int getMaxResults() { return maxResults; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HistoryQuery)) return false;
        HistoryQuery that = (HistoryQuery) o;
        return maxResults == that.maxResults
                && Objects.equals(jobName, that.jobName)
                && Objects.equals(from, that.from)
                && Objects.equals(to, that.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobName, from, to, maxResults);
    }

    @Override
    public String toString() {
        return "HistoryQuery{jobName='" + jobName + "', from=" + from
                + ", to=" + to + ", maxResults=" + maxResults + "}";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String jobName;
        private Instant from;
        private Instant to = Instant.now();
        private int maxResults = DEFAULT_MAX_RESULTS;

        public Builder jobName(String jobName) { this.jobName = jobName; return this; }
        public Builder from(Instant from) { this.from = from; return this; }
        public Builder to(Instant to) { this.to = to; return this; }
        public Builder maxResults(int maxResults) {
            if (maxResults <= 0) throw new IllegalArgumentException("maxResults must be positive");
            this.maxResults = maxResults;
            return this;
        }

        public HistoryQuery build() {
            if (jobName == null || jobName.isBlank()) {
                throw new IllegalStateException("jobName is required");
            }
            if (from == null) {
                throw new IllegalStateException("from is required");
            }
            if (from.isAfter(to)) {
                throw new IllegalStateException("'from' must not be after 'to'");
            }
            return new HistoryQuery(this);
        }
    }
}
