package com.spring.scheduler.jobs.impl;

import com.spring.scheduler.jobs.Job;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Sample job that demonstrates successful execution with logging.
 *
 * @author lewisjones
 */
@Component
@Slf4j
public class SuccessJob implements Job
{
    private static final String JOB_NAME = "success-job";
    private static final String JOB_DESCRIPTION = "A job that executes successfully and logs a message";

    /**
     * Executes the job by logging a success message.
     * This simulates a simple job that completes without errors.
     */
    @Override
    public void execute()
    {
        log.info( "SuccessJob is executing..." );
        log.info( "SuccessJob completed successfully" );
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
     * Called when job execution completes successfully.
     */
    @Override
    public void onSuccess()
    {
        log.info( "SuccessJob.onSuccess() callback invoked" );
    }
}

