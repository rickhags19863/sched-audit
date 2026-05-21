package com.schedaudit.scheduler;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Parses simplified cron expressions and computes next/previous expected run times.
 * Supports standard 5-field cron: minute hour dayOfMonth month dayOfWeek
 */
public class CronScheduleParser {

    private static final int FIELD_MINUTE = 0;
    private static final int FIELD_HOUR = 1;
    private static final int FIELD_DOM = 2;
    private static final int FIELD_MONTH = 3;
    private static final int FIELD_DOW = 4;

    private final String expression;
    private final String[] fields;

    public CronScheduleParser(String expression) {
        if (expression == null || expression.isBlank()) {
            throw new IllegalArgumentException("Cron expression must not be null or blank");
        }
        this.expression = expression.trim();
        this.fields = this.expression.split("\\s+");
        if (fields.length != 5) {
            throw new IllegalArgumentException(
                "Cron expression must have exactly 5 fields, got: " + fields.length);
        }
    }

    public String getExpression() {
        return expression;
    }

    /**
     * Returns the most recent expected run time at or before the given reference time.
     */
    public LocalDateTime previousRunTime(LocalDateTime reference) {
        LocalDateTime candidate = reference.withSecond(0).withNano(0);
        // Search backwards up to 366 days
        for (int i = 0; i < 366 * 24 * 60; i++) {
            if (matches(candidate)) {
                return candidate;
            }
            candidate = candidate.minusMinutes(1);
        }
        throw new IllegalStateException("Could not find previous run time for expression: " + expression);
    }

    /**
     * Returns the next expected run time strictly after the given reference time.
     */
    public LocalDateTime nextRunTime(LocalDateTime reference) {
        LocalDateTime candidate = reference.withSecond(0).withNano(0).plusMinutes(1);
        for (int i = 0; i < 366 * 24 * 60; i++) {
            if (matches(candidate)) {
                return candidate;
            }
            candidate = candidate.plusMinutes(1);
        }
        throw new IllegalStateException("Could not find next run time for expression: " + expression);
    }

    public boolean matches(LocalDateTime dt) {
        return fieldMatches(fields[FIELD_MINUTE], dt.getMinute())
            && fieldMatches(fields[FIELD_HOUR], dt.getHour())
            && fieldMatches(fields[FIELD_DOM], dt.getDayOfMonth())
            && fieldMatches(fields[FIELD_MONTH], dt.getMonthValue())
            && fieldMatches(fields[FIELD_DOW], dt.getDayOfWeek().getValue() % 7);
    }

    private boolean fieldMatches(String field, int value) {
        if ("*".equals(field)) return true;
        if (field.contains(",")) {
            return Arrays.stream(field.split(","))
                .anyMatch(part -> fieldMatches(part.trim(), value));
        }
        if (field.contains("/")) {
            String[] parts = field.split("/");
            int step = Integer.parseInt(parts[1]);
            int start = "*".equals(parts[0]) ? 0 : Integer.parseInt(parts[0]);
            return value >= start && (value - start) % step == 0;
        }
        if (field.contains("-")) {
            String[] parts = field.split("-");
            int lo = Integer.parseInt(parts[0]);
            int hi = Integer.parseInt(parts[1]);
            return value >= lo && value <= hi;
        }
        return Integer.parseInt(field) == value;
    }
}
