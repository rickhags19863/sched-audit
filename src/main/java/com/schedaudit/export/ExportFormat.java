package com.schedaudit.export;

/**
 * Supported export formats for audit reports.
 */
public enum ExportFormat {

    /** Comma-separated values format. */
    CSV,

    /** JSON format. */
    JSON;

    /**
     * Parses a format name (case-insensitive) to an ExportFormat.
     *
     * @param name the format name
     * @return the matching ExportFormat
     * @throws IllegalArgumentException if the name is not recognised
     */
    public static ExportFormat fromString(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Format name must not be null or blank");
        }
        for (ExportFormat f : values()) {
            if (f.name().equalsIgnoreCase(name.trim())) {
                return f;
            }
        }
        throw new IllegalArgumentException("Unknown export format: '" + name + "'");
    }
}
