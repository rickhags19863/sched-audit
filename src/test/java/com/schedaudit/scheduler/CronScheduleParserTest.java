package com.schedaudit.scheduler;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CronScheduleParserTest {

    @Test
    void constructor_throwsOnNullExpression() {
        assertThrows(IllegalArgumentException.class, () -> new CronScheduleParser(null));
    }

    @Test
    void constructor_throwsOnWrongFieldCount() {
        assertThrows(IllegalArgumentException.class, () -> new CronScheduleParser("* * * *"));
    }

    @Test
    void matches_wildcardExpression_alwaysTrue() {
        CronScheduleParser parser = new CronScheduleParser("* * * * *");
        assertTrue(parser.matches(LocalDateTime.of(2024, 6, 15, 10, 30)));
        assertTrue(parser.matches(LocalDateTime.of(2024, 1, 1, 0, 0)));
    }

    @Test
    void matches_specificMinuteAndHour() {
        CronScheduleParser parser = new CronScheduleParser("30 9 * * *");
        assertTrue(parser.matches(LocalDateTime.of(2024, 6, 15, 9, 30)));
        assertFalse(parser.matches(LocalDateTime.of(2024, 6, 15, 9, 31)));
        assertFalse(parser.matches(LocalDateTime.of(2024, 6, 15, 10, 30)));
    }

    @Test
    void matches_stepExpression() {
        CronScheduleParser parser = new CronScheduleParser("*/15 * * * *");
        assertTrue(parser.matches(LocalDateTime.of(2024, 6, 15, 10, 0)));
        assertTrue(parser.matches(LocalDateTime.of(2024, 6, 15, 10, 15)));
        assertTrue(parser.matches(LocalDateTime.of(2024, 6, 15, 10, 30)));
        assertTrue(parser.matches(LocalDateTime.of(2024, 6, 15, 10, 45)));
        assertFalse(parser.matches(LocalDateTime.of(2024, 6, 15, 10, 7)));
    }

    @Test
    void matches_rangeExpression() {
        CronScheduleParser parser = new CronScheduleParser("0 9-17 * * *");
        assertTrue(parser.matches(LocalDateTime.of(2024, 6, 15, 9, 0)));
        assertTrue(parser.matches(LocalDateTime.of(2024, 6, 15, 17, 0)));
        assertFalse(parser.matches(LocalDateTime.of(2024, 6, 15, 8, 0)));
        assertFalse(parser.matches(LocalDateTime.of(2024, 6, 15, 18, 0)));
    }

    @Test
    void matches_listExpression() {
        CronScheduleParser parser = new CronScheduleParser("0 8,12,18 * * *");
        assertTrue(parser.matches(LocalDateTime.of(2024, 6, 15, 8, 0)));
        assertTrue(parser.matches(LocalDateTime.of(2024, 6, 15, 12, 0)));
        assertTrue(parser.matches(LocalDateTime.of(2024, 6, 15, 18, 0)));
        assertFalse(parser.matches(LocalDateTime.of(2024, 6, 15, 10, 0)));
    }

    @Test
    void nextRunTime_returnsCorrectNextTime() {
        CronScheduleParser parser = new CronScheduleParser("0 * * * *");
        LocalDateTime ref = LocalDateTime.of(2024, 6, 15, 10, 30);
        LocalDateTime next = parser.nextRunTime(ref);
        assertEquals(LocalDateTime.of(2024, 6, 15, 11, 0), next);
    }

    @Test
    void previousRunTime_returnsCorrectPreviousTime() {
        CronScheduleParser parser = new CronScheduleParser("0 * * * *");
        LocalDateTime ref = LocalDateTime.of(2024, 6, 15, 10, 45);
        LocalDateTime prev = parser.previousRunTime(ref);
        assertEquals(LocalDateTime.of(2024, 6, 15, 10, 0), prev);
    }

    @Test
    void previousRunTime_exactMatch_returnsSameTime() {
        CronScheduleParser parser = new CronScheduleParser("0 10 * * *");
        LocalDateTime ref = LocalDateTime.of(2024, 6, 15, 10, 0);
        assertEquals(ref, parser.previousRunTime(ref));
    }

    @Test
    void getExpression_returnsOriginal() {
        String expr = "30 6 * * 1";
        CronScheduleParser parser = new CronScheduleParser(expr);
        assertEquals(expr, parser.getExpression());
    }
}
