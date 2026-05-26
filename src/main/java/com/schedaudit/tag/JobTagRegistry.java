package com.schedaudit.tag;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for managing tags associated with scheduled jobs.
 * Tags allow grouping and filtering jobs by arbitrary labels (e.g., "critical", "nightly", "etl").
 */
public class JobTagRegistry {

    private final Map<String, Set<String>> jobToTags = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> tagToJobs = new ConcurrentHashMap<>();

    /**
     * Assigns one or more tags to a job.
     *
     * @param jobId the job identifier
     * @param tags  tags to associate with the job
     */
    public void tag(String jobId, String... tags) {
        if (jobId == null || jobId.isBlank()) {
            throw new IllegalArgumentException("jobId must not be null or blank");
        }
        Set<String> jobTags = jobToTags.computeIfAbsent(jobId, k -> ConcurrentHashMap.newKeySet());
        for (String tag : tags) {
            if (tag != null && !tag.isBlank()) {
                jobTags.add(tag.trim().toLowerCase());
                tagToJobs.computeIfAbsent(tag.trim().toLowerCase(), k -> ConcurrentHashMap.newKeySet()).add(jobId);
            }
        }
    }

    /**
     * Removes a specific tag from a job.
     *
     * @param jobId the job identifier
     * @param tag   the tag to remove
     */
    public void removeTag(String jobId, String tag) {
        if (jobId == null || tag == null) return;
        String normalised = tag.trim().toLowerCase();
        Set<String> jobTags = jobToTags.get(jobId);
        if (jobTags != null) {
            jobTags.remove(normalised);
            if (jobTags.isEmpty()) jobToTags.remove(jobId);
        }
        Set<String> tagJobs = tagToJobs.get(normalised);
        if (tagJobs != null) {
            tagJobs.remove(jobId);
            if (tagJobs.isEmpty()) tagToJobs.remove(normalised);
        }
    }

    /**
     * Returns all tags assigned to a job.
     */
    public Set<String> getTagsForJob(String jobId) {
        return Collections.unmodifiableSet(jobToTags.getOrDefault(jobId, Collections.emptySet()));
    }

    /**
     * Returns all job IDs that carry a given tag.
     */
    public Set<String> getJobsForTag(String tag) {
        if (tag == null) return Collections.emptySet();
        return Collections.unmodifiableSet(tagToJobs.getOrDefault(tag.trim().toLowerCase(), Collections.emptySet()));
    }

    /**
     * Returns true if the job has all of the specified tags.
     */
    public boolean hasAllTags(String jobId, String... tags) {
        Set<String> jobTags = jobToTags.getOrDefault(jobId, Collections.emptySet());
        for (String tag : tags) {
            if (tag == null || !jobTags.contains(tag.trim().toLowerCase())) return false;
        }
        return true;
    }

    /** Removes all tag associations for a job (e.g., when a job is deregistered). */
    public void clearJob(String jobId) {
        Set<String> removed = jobToTags.remove(jobId);
        if (removed != null) {
            removed.forEach(tag -> {
                Set<String> jobs = tagToJobs.get(tag);
                if (jobs != null) {
                    jobs.remove(jobId);
                    if (jobs.isEmpty()) tagToJobs.remove(tag);
                }
            });
        }
    }

    /** Returns all known tags across all jobs. */
    public Set<String> allTags() {
        return Collections.unmodifiableSet(tagToJobs.keySet());
    }
}
