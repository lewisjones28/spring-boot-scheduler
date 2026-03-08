package com.spring.scheduler.jobs.impl;

import com.spring.scheduler.jobs.Job;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Sample job that demonstrates failure handling with exception throwing.
 *
 * @author lewisjones
 */
@Component
@Slf4j
public class FailureJob implements Job
{
    private static final String JOB_NAME = "failure-job";
    private static final String JOB_DESCRIPTION = "A job that intentionally fails to demonstrate error handling";

    /**
     * Executes the job by intentionally throwing an exception.
     *
     * @throws RuntimeException always thrown to demonstrate failure handling
     */
    @Override
    public void execute()
    {
        log.info( "FailureJob is executing..." );
        log.warn( "FailureJob intentionally throwing an exception to demonstrate error handling" );
        throw new RuntimeException( "Simulated job failure: database connection timeout" );
    }

    /**
     * Gets the unique name of this job.
     *
     * @return the job name
     */
    @Override
    public String getJobName()
    {
        return JOB_NAME;
    }

    /**
     * Gets the description of this job.
     *
     * @return the job description
     */
    @Override
    public String getJobDescription()
    {
        return JOB_DESCRIPTION;
    }

    /**
     * Gets the execution interval in milliseconds.
     *
     * @return the interval in milliseconds
     */
    @Override
    public long getIntervalMs()
    {
        return DEFAULT_INTERVAL_MS;
    }

    /**
     * Called when job execution fails.
     *
     * @param error the exception that caused the failure
     */
    @Override
    public void onFailure( final Throwable error )
    {
        log.error( "FailureJob.onFailure() callback invoked with error: {}", error.getMessage(), error );
    }
}

