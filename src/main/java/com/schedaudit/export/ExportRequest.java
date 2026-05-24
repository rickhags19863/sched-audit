package com.schedaudit.export;

import java.time.LocalDateTime;

/**
 * Encapsulates the parameters for a report export request.
 */
public class ExportRequest {

    private final String jobName;        // null means all jobs
    private final ExportFormat format;
    private final LocalDateTime from;
    private final LocalDateTime to;

    public ExportRequest(String jobName, ExportFormat format,
                         LocalDateTime from, LocalDateTime to) {
        if (format == null) {
            throw new IllegalArgumentException("ExportFormat must not be null");
        }
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("'from' must not be after 'to'");
        }
        this.jobName = jobName;
        this.format = format;
        this.from = from;
        this.to = to;
    }

    /** Convenience constructor for all jobs with no time restriction. */
    public ExportRequest(ExportFormat format) {
        this(null, format, null, null);
    }

    public String getJobName() { return jobName; }
    public ExportFormat getFormat() { return format; }
    public LocalDateTime getFrom() { return from; }
    public LocalDateTime getTo() { return to; }

    public boolean hasJobFilter() { return jobName != null && !jobName.isBlank(); }
    public boolean hasTimeRange() { return from != null && to != null; }

    @Override
    public String toString() {
        return String.format("ExportRequest{jobName='%s', format=%s, from=%s, to=%s}",
                jobName, format, from, to);
    }
}
