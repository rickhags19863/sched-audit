package com.schedaudit.tag;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class JobTagRegistryTest {

    private JobTagRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new JobTagRegistry();
    }

    @Test
    void tagAndRetrieve() {
        registry.tag("job-1", "critical", "nightly");
        Set<String> tags = registry.getTagsForJob("job-1");
        assertTrue(tags.contains("critical"));
        assertTrue(tags.contains("nightly"));
        assertEquals(2, tags.size());
    }

    @Test
    void tagsAreNormalisedToLowercase() {
        registry.tag("job-2", "ETL", "  Nightly  ");
        assertTrue(registry.getTagsForJob("job-2").contains("etl"));
        assertTrue(registry.getTagsForJob("job-2").contains("nightly"));
    }

    @Test
    void getJobsForTag_returnsCorrectJobs() {
        registry.tag("job-1", "critical");
        registry.tag("job-2", "critical");
        registry.tag("job-3", "nightly");
        Set<String> criticalJobs = registry.getJobsForTag("critical");
        assertTrue(criticalJobs.contains("job-1"));
        assertTrue(criticalJobs.contains("job-2"));
        assertFalse(criticalJobs.contains("job-3"));
    }

    @Test
    void removeTag_removesOnlySpecifiedTag() {
        registry.tag("job-1", "critical", "nightly");
        registry.removeTag("job-1", "critical");
        assertFalse(registry.getTagsForJob("job-1").contains("critical"));
        assertTrue(registry.getTagsForJob("job-1").contains("nightly"));
        assertFalse(registry.getJobsForTag("critical").contains("job-1"));
    }

    @Test
    void hasAllTags_returnsTrueWhenAllPresent() {
        registry.tag("job-1", "critical", "nightly", "etl");
        assertTrue(registry.hasAllTags("job-1", "critical", "nightly"));
        assertFalse(registry.hasAllTags("job-1", "critical", "missing"));
    }

    @Test
    void clearJob_removesAllAssociations() {
        registry.tag("job-1", "critical", "nightly");
        registry.clearJob("job-1");
        assertTrue(registry.getTagsForJob("job-1").isEmpty());
        assertFalse(registry.getJobsForTag("critical").contains("job-1"));
        assertFalse(registry.getJobsForTag("nightly").contains("job-1"));
    }

    @Test
    void allTags_returnsAllKnownTags() {
        registry.tag("job-1", "critical");
        registry.tag("job-2", "nightly");
        Set<String> all = registry.allTags();
        assertTrue(all.contains("critical"));
        assertTrue(all.contains("nightly"));
    }

    @Test
    void tagWithBlankJobId_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> registry.tag("", "critical"));
        assertThrows(IllegalArgumentException.class, () -> registry.tag(null, "critical"));
    }

    @Test
    void getJobsForUnknownTag_returnsEmptySet() {
        assertTrue(registry.getJobsForTag("nonexistent").isEmpty());
    }

    @Test
    void returnedSetsAreUnmodifiable() {
        registry.tag("job-1", "critical");
        Set<String> tags = registry.getTagsForJob("job-1");
        assertThrows(UnsupportedOperationException.class, () -> tags.add("new-tag"));
    }
}
