package com.spring.scheduler.jobs.impl;

import com.spring.scheduler.jobs.Job;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/**
 * Sample job that demonstrates a long-running task.
 * This job executes for approximately 1 hour, simulating
 * a computationally intensive or resource-heavy operation.
 *
 * @author lewisjones
 */
@Component
@Slf4j
public class LongRunningJob implements Job
{
    private static final String JOB_NAME = "long-running-job";
    private static final String JOB_DESCRIPTION = "A long-running job that takes approximately 1 hour to complete";
    private static final long INTERVAL_MS = 3600000L; // 1 hour
    private static final long EXECUTION_DURATION_MS = 3600000L; // 1 hour (3600 seconds)
    private static final long PROGRESS_LOG_INTERVAL_SECONDS = 600L; // Log progress every 10 minutes
    private static final long SLEEP_INTERVAL_MS = 1000L; // Sleep for 1 second between checks
    private static final long SECONDS_PER_MILLISECOND = 1000L;

    /**
     * Executes the job by running a 1-hour timer.
     */
    @Override
    public void execute()
    {
        log.info( "LongRunningJob is executing... (estimated duration: 1 hour)" );
        final Instant startTime = Instant.now();

        try
        {
            while ( Instant.now().isBefore( startTime.plusMillis( EXECUTION_DURATION_MS ) ) )
            {
                final Duration elapsed = Duration.between( startTime, Instant.now() );
                final long elapsedSeconds = elapsed.getSeconds();

                // Log progress every 10 minutes
                if ( elapsedSeconds % PROGRESS_LOG_INTERVAL_SECONDS == 0 )
                {
                    log.info( "LongRunningJob progress: {} seconds elapsed / {} seconds total", elapsedSeconds,
                        EXECUTION_DURATION_MS / SECONDS_PER_MILLISECOND );
                }

                // Sleep for a short interval to avoid busy-waiting
                Thread.sleep( SLEEP_INTERVAL_MS );
            }

            log.info( "LongRunningJob completed successfully after 1 hour" );
        }
        catch ( final InterruptedException e )
        {
            Thread.currentThread().interrupt();
            log.warn( "LongRunningJob was interrupted after {} ms",
                Duration.between( startTime, Instant.now() ).toMillis() );
            throw new RuntimeException( "LongRunningJob execution was interrupted", e );
        }
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
     * @return the interval in milliseconds (1 hour)
     */
    @Override
    public long getIntervalMs()
    {
        return INTERVAL_MS;
    }

    /**
     * Called when job execution completes successfully.
     */
    @Override
    public void onSuccess()
    {
        log.info( "LongRunningJob.onSuccess() callback invoked - long-running task completed" );
    }

    /**
     * Called when job execution fails.
     *
     * @param error the exception that caused the failure
     */
    @Override
    public void onFailure( final Throwable error )
    {
        log.error( "LongRunningJob.onFailure() callback invoked with error: {}", error.getMessage(), error );
    }
}

