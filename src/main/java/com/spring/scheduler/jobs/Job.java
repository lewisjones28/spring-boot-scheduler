package com.spring.scheduler.jobs;

/**
 * Interface for schedulable jobs that can be executed by the central scheduler.
 *
 * @author lewisjones
 */
public interface Job
{

    long DEFAULT_INTERVAL_MS = 3600000L; // 1 hour default interval

    /**
     * Executes the job logic.
     */
    void execute();

    /**
     * Gets the unique name of this job.
     *
     * @return the job name
     */
    String getJobName();

    /**
     * Gets the description of this job.
     *
     * @return the job description
     */
    String getJobDescription();

    /**
     * Gets the execution interval in milliseconds.
     *
     * @return the interval in milliseconds
     */
    long getIntervalMs();

    /**
     * Called when job execution completes successfully.
     */
    default void onSuccess()
    {
        // no-op by default
    }

    /**
     * Called when job execution fails.
     *
     * @param error the exception that caused the failure
     */
    default void onFailure( final Throwable error )
    {
        // no-op by default
    }
}
