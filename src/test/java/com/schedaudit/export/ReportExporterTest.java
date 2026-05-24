package com.schedaudit.export;

import com.schedaudit.audit.AuditReport;
import com.schedaudit.model.JobExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReportExporterTest {

    private AuditReport report;
    private JobExecution exec1;
    private JobExecution exec2;

    @BeforeEach
    void setUp() {
        exec1 = new JobExecution("backup-job", "SUCCESS",
                LocalDateTime.of(2024, 6, 1, 2, 0),
                LocalDateTime.of(2024, 6, 1, 2, 5), 300_000L);
        exec2 = new JobExecution("cleanup-job", "FAILED",
                LocalDateTime.of(2024, 6, 1, 3, 0),
                LocalDateTime.of(2024, 6, 1, 3, 1), 60_000L);
        report = new AuditReport(List.of(exec1, exec2));
    }

    @Test
    void csvExportContainsHeader() throws Exception {
        ReportExporter exporter = new ReportExporter(ExportFormat.CSV);
        StringWriter sw = new StringWriter();
        exporter.export(report, sw);
        assertTrue(sw.toString().startsWith("jobName,status,startTime,endTime,durationMs"));
    }

    @Test
    void csvExportContainsJobRows() throws Exception {
        ReportExporter exporter = new ReportExporter(ExportFormat.CSV);
        StringWriter sw = new StringWriter();
        exporter.export(report, sw);
        String csv = sw.toString();
        assertTrue(csv.contains("backup-job"));
        assertTrue(csv.contains("SUCCESS"));
        assertTrue(csv.contains("cleanup-job"));
        assertTrue(csv.contains("FAILED"));
    }

    @Test
    void jsonExportContainsExecutionsKey() throws Exception {
        ReportExporter exporter = new ReportExporter(ExportFormat.JSON);
        StringWriter sw = new StringWriter();
        exporter.export(report, sw);
        String json = sw.toString();
        assertTrue(json.contains("\"executions\":"));
        assertTrue(json.contains("backup-job"));
        assertTrue(json.contains("cleanup-job"));
    }

    @Test
    void nullFormatThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new ReportExporter(null));
    }

    @Test
    void nullReportThrowsException() {
        ReportExporter exporter = new ReportExporter(ExportFormat.CSV);
        assertThrows(IllegalArgumentException.class,
                () -> exporter.export(null, new StringWriter()));
    }

    @Test
    void getFormatReturnsCorrectFormat() {
        ReportExporter exporter = new ReportExporter(ExportFormat.JSON);
        assertEquals(ExportFormat.JSON, exporter.getFormat());
    }
}
