package com.spring.scheduler.common;

/**
 * Job status enumeration. Defines the possible states of a scheduled job, such as idle, running, completed, or failed.
 *
 * @author lewisjones
 */
public enum JobStatus
{
    /**
     * Job is idle and ready to run.
     */
    IDLE,

    /**
     * Job is currently running.
     */
    RUNNING,

    /**
     * Job completed successfully.
     */
    COMPLETED,

    /**
     * Job failed with an error.
     */
    FAILED
}

