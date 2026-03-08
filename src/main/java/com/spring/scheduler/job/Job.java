package com.spring.scheduler.job;

/**
 * Interface for schedulable jobs that can be executed by the central scheduler.
 *
 * @author lewisjones
 */
public interface Job
{
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
}
