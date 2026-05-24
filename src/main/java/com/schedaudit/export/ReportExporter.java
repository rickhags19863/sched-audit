package com.schedaudit.export;

import com.schedaudit.audit.AuditReport;
import com.schedaudit.model.JobExecution;

import java.io.IOException;
import java.io.Writer;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Exports audit reports to various formats (CSV, JSON).
 */
public class ReportExporter {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final ExportFormat format;

    public ReportExporter(ExportFormat format) {
        if (format == null) {
            throw new IllegalArgumentException("Export format must not be null");
        }
        this.format = format;
    }

    public void export(AuditReport report, Writer writer) throws IOException {
        if (report == null) {
            throw new IllegalArgumentException("AuditReport must not be null");
        }
        switch (format) {
            case CSV:
                exportCsv(report, writer);
                break;
            case JSON:
                exportJson(report, writer);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported format: " + format);
        }
    }

    private void exportCsv(AuditReport report, Writer writer) throws IOException {
        writer.write("jobName,status,startTime,endTime,durationMs\n");
        for (JobExecution exec : report.getExecutions()) {
            writer.write(String.format("%s,%s,%s,%s,%d\n",
                    exec.getJobName(),
                    exec.getStatus(),
                    exec.getStartTime() != null ? exec.getStartTime().format(FORMATTER) : "",
                    exec.getEndTime() != null ? exec.getEndTime().format(FORMATTER) : "",
                    exec.getDurationMs()));
        }
    }

    private void exportJson(AuditReport report, Writer writer) throws IOException {
        List<JobExecution> executions = report.getExecutions();
        writer.write("{\"executions\":[\n");
        for (int i = 0; i < executions.size(); i++) {
            JobExecution exec = executions.get(i);
            writer.write(String.format(
                    "  {\"jobName\":\"%s\",\"status\":\"%s\",\"startTime\":\"%s\",\"endTime\":\"%s\",\"durationMs\":%d}",
                    exec.getJobName(),
                    exec.getStatus(),
                    exec.getStartTime() != null ? exec.getStartTime().format(FORMATTER) : "",
                    exec.getEndTime() != null ? exec.getEndTime().format(FORMATTER) : "",
                    exec.getDurationMs()));
            if (i < executions.size() - 1) writer.write(",");
            writer.write("\n");
        }
        writer.write("]}");
    }

    public ExportFormat getFormat() {
        return format;
    }
}
