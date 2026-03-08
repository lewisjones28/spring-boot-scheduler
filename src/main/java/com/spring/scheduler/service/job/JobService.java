// CSOFF: RegexpSinglelineJava
package com.spring.scheduler.service.job;

import com.spring.scheduler.common.job.JobExecutionStatus;
import com.spring.scheduler.common.job.JobStatus;
import com.spring.scheduler.domain.job.JobConfig;
import com.spring.scheduler.domain.job.JobExecution;
import com.spring.scheduler.exception.job.JobExecutionException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing scheduled job configurations and execution state.
 *
 * @author lewisjones
 */
@Service
public class JobService
{
    private static final long JOB_RETRY_DELAY_MINUTES = 5L;
    private final JobConfigService jobConfigService;
    private final JobExecutionService jobExecutionService;

    /**
     * Constructs a new {@link JobService}.
     *
     * @param jobConfigService    job configuration service
     * @param jobExecutionService job execution service
     */
    public JobService( final JobConfigService jobConfigService, final JobExecutionService jobExecutionService )
    {
        this.jobConfigService = jobConfigService;
        this.jobExecutionService = jobExecutionService;
    }

    /**
     * Converts an exception stack trace to text.
     *
     * @param error throwable to render
     * @return stack trace string
     */
    private static String stackTraceFor( final Throwable error )
    {
        final StringWriter writer = new StringWriter();
        error.printStackTrace( new PrintWriter( writer ) );
        return writer.toString();
    }

    /**
     * Resolves execution node identifier from environment.
     *
     * @param jobName fallback job name
     * @return pod or host name when available
     */
    private static String resolveExecutedName( final String jobName )
    {
        final String podName = System.getenv( "POD_NAME" );
        if ( podName != null && !podName.isBlank() )
        {
            return podName;
        }

        final String hostname = System.getenv( "HOSTNAME" );
        if ( hostname != null && !hostname.isBlank() )
        {
            return hostname;
        }

        return jobName != null && !jobName.isBlank() ? jobName + "-1" : null;
    }

    /**
     * Finds all jobs that are due to run.
     *
     * @return the list of jobs due to run
     */
    @Transactional( readOnly = true )
    public List<JobConfig> findJobsDueToRun()
    {
        return jobConfigService.findJobsDueToRun();
    }

    /**
     * Attempts to claim a job for execution.
     *
     * @param jobName the job name
     * @return true if the job was successfully claimed, false otherwise
     */
    @Transactional
    public boolean claimJob( final String jobName )
    {
        return jobConfigService.claimJob( jobName );
    }

    /**
     * Starts execution of a job and persists an execution record.
     *
     * @param jobName the job name
     * @return persisted execution record if job exists, otherwise empty
     */
    @Transactional
    public Optional<JobExecution> startExecution( final String jobName )
    {
        final Optional<JobConfig> jobOpt = jobConfigService.findByName( jobName );
        if ( jobOpt.isEmpty() )
        {
            return Optional.empty();
        }

        final JobExecution execution =
            new JobExecution( jobOpt.get(), LocalDateTime.now(), resolveExecutedName( jobName ) );
        return Optional.of( jobExecutionService.save( execution ) );
    }

    /**
     * Marks a job as completed and schedules the next run.
     *
     * @param jobName   the job name
     * @param execution execution record to update, if present
     */
    @Transactional
    public void markJobCompleted( final String jobName, final JobExecution execution )
    {
        final Optional<JobConfig> jobOpt = jobConfigService.findByName( jobName );
        if ( jobOpt.isEmpty() )
        {
            return;
        }

        final JobConfig job = jobOpt.get();
        job.setStatus( JobStatus.IDLE );
        job.setNextRunTime( LocalDateTime.now().plus( Duration.ofMillis( job.getIntervalMillis() ) ) );
        jobConfigService.save( job );

        if ( execution != null )
        {
            execution.setStatus( JobExecutionStatus.SUCCESS );
            execution.setCompletedAt( LocalDateTime.now() );
            jobExecutionService.save( execution );
        }
    }

    /**
     * Marks a job as failed and schedules a retry.
     *
     * @param jobName   the job name
     * @param error     the error that caused failure
     * @param execution execution record to update, if present
     */
    @Transactional
    public void markJobFailed( final String jobName, final Throwable error, final JobExecution execution )
    {
        final Optional<JobConfig> jobOpt = jobConfigService.findByName( jobName );
        if ( jobOpt.isEmpty() )
        {
            throw new JobExecutionException( "Job [" + jobName + "] not found in configuration" );
        }

        final JobConfig job = jobOpt.get();
        job.setStatus( JobStatus.IDLE );
        job.setNextRunTime( LocalDateTime.now().plusMinutes( JOB_RETRY_DELAY_MINUTES ) );
        jobConfigService.save( job );

        if ( execution != null )
        {
            execution.setStatus( JobExecutionStatus.FAILED );
            execution.setCompletedAt( LocalDateTime.now() );
            execution.setErrorMessage( error != null ? error.getMessage() : null );
            execution.setErrorStacktrace( error != null ? stackTraceFor( error ) : null );
            jobExecutionService.save( execution );
        }
    }

    /**
     * Creates or updates a job configuration.
     *
     * @param jobConfig the job configuration
     * @return saved job configuration
     */
    @Transactional
    public JobConfig save( final JobConfig jobConfig )
    {
        return jobConfigService.save( jobConfig );
    }
}
