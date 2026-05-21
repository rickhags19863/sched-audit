package com.schedaudit.scheduler;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.regex.Pattern;

/**
 * Parses simple cron expressions and computes the next/previous expected run times.
 * Supports standard 5-field cron expressions: minute hour dom month dow
 */
public class CronScheduleParser {

    private static final Pattern CRON_PATTERN =
            Pattern.compile("^(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)$");

    private final String expression;

    public CronScheduleParser(String expression) {
        if (expression == null || !CRON_PATTERN.matcher(expression.trim()).matches()) {
            throw new IllegalArgumentException("Invalid cron expression: " + expression);
        }
        this.expression = expression.trim();
    }

    public String getExpression() {
        return expression;
    }

    /**
     * Returns the expected interval in minutes for simple @every-N-minutes patterns.
     * Only handles expressions of the form: *\/N * * * *
     */
    public long getIntervalMinutes() {
        String[] fields = expression.split("\\s+");
        String minuteField = fields[0];
        if (minuteField.startsWith("*/")) {
            try {
                return Long.parseLong(minuteField.substring(2));
            } catch (NumberFormatException e) {
                throw new UnsupportedOperationException("Cannot determine interval for expression: " + expression);
            }
        }
        if (minuteField.matches("\\d+") && isWildcard(fields[1])) {
            return 60;
        }
        throw new UnsupportedOperationException("Cannot determine interval for expression: " + expression);
    }

    /**
     * Computes the latest expected run time at or before the given reference time,
     * based on the interval derived from the cron expression.
     */
    public LocalDateTime lastExpectedRunBefore(LocalDateTime reference) {
        long intervalMinutes = getIntervalMinutes();
        long minutesSinceEpoch = ChronoUnit.MINUTES.between(LocalDateTime.of(1970, 1, 1, 0, 0), reference);
        long lastSlot = (minutesSinceEpoch / intervalMinutes) * intervalMinutes;
        return LocalDateTime.of(1970, 1, 1, 0, 0).plusMinutes(lastSlot);
    }

    /**
     * Returns the next expected run time after the given reference time.
     */
    public LocalDateTime nextExpectedRunAfter(LocalDateTime reference) {
        long intervalMinutes = getIntervalMinutes();
        return lastExpectedRunBefore(reference).plusMinutes(intervalMinutes);
    }

    private boolean isWildcard(String field) {
        return "*".equals(field);
    }
}
