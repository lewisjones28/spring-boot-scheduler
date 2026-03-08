package com.spring.scheduler.common;

/**
 * Execution status values. Defines the possible states of a job execution, such as running, success, or failed.
 *
 * @author lewisjones
 */
public enum ExecutionStatus
{

    /**
     * Execution has started and is in progress.
     */
    RUNNING,

    /**
     * Execution finished successfully.
     */
    SUCCESS,

    /**
     * Execution finished with an error.
     */
    FAILED
}
