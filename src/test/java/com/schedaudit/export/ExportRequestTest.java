package com.schedaudit.export;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ExportRequestTest {

    @Test
    void defaultConstructorSetsFormatAndNullFilters() {
        ExportRequest req = new ExportRequest(ExportFormat.CSV);
        assertEquals(ExportFormat.CSV, req.getFormat());
        assertNull(req.getJobName());
        assertNull(req.getFrom());
        assertNull(req.getTo());
        assertFalse(req.hasJobFilter());
        assertFalse(req.hasTimeRange());
    }

    @Test
    void fullConstructorRetainsAllFields() {
        LocalDateTime from = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime to   = LocalDateTime.of(2024, 1, 31, 23, 59);
        ExportRequest req = new ExportRequest("my-job", ExportFormat.JSON, from, to);
        assertEquals("my-job", req.getJobName());
        assertEquals(ExportFormat.JSON, req.getFormat());
        assertEquals(from, req.getFrom());
        assertEquals(to, req.getTo());
        assertTrue(req.hasJobFilter());
        assertTrue(req.hasTimeRange());
    }

    @Test
    void nullFormatThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> new ExportRequest("job", null, null, null));
    }

    @Test
    void fromAfterToThrowsException() {
        LocalDateTime from = LocalDateTime.of(2024, 6, 10, 0, 0);
        LocalDateTime to   = LocalDateTime.of(2024, 6, 1, 0, 0);
        assertThrows(IllegalArgumentException.class,
                () -> new ExportRequest("job", ExportFormat.CSV, from, to));
    }

    @Test
    void exportFormatFromStringIsCaseInsensitive() {
        assertEquals(ExportFormat.CSV,  ExportFormat.fromString("csv"));
        assertEquals(ExportFormat.JSON, ExportFormat.fromString("JSON"));
        assertEquals(ExportFormat.CSV,  ExportFormat.fromString("Csv"));
    }

    @Test
    void exportFormatFromStringUnknownThrows() {
        assertThrows(IllegalArgumentException.class, () -> ExportFormat.fromString("xml"));
        assertThrows(IllegalArgumentException.class, () -> ExportFormat.fromString(""));
        assertThrows(IllegalArgumentException.class, () -> ExportFormat.fromString(null));
    }

    @Test
    void toStringContainsRelevantInfo() {
        ExportRequest req = new ExportRequest("report-job", ExportFormat.CSV, null, null);
        String str = req.toString();
        assertTrue(str.contains("report-job"));
        assertTrue(str.contains("CSV"));
    }
}
