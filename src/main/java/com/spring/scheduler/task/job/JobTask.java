package com.spring.scheduler.task.job;

import com.spring.scheduler.domain.job.JobConfig;
import com.spring.scheduler.exception.job.JobExecutionException;
import com.spring.scheduler.jobs.Job;
import com.spring.scheduler.service.job.JobService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Central scheduler that reads job configurations from the database and delegates execution
 * to registered job implementations. This prevents duplicate job executions across multiple pods
 * by using database-level locking.
 *
 * @author lewisjones
 */
@Component
@Slf4j
public class JobTask
{
    private final JobService jobService;
    private final Map<String, Job> jobRegistry;

    /**
     * Constructs a new {@link JobTask}.
     *
     * @param jobService the job service for persistence operations
     * @param jobs       the list of all schedulable jobs registered in the Spring context
     */
    public JobTask( final JobService jobService, final List<Job> jobs )
    {
        this.jobService = jobService;
        this.jobRegistry = jobs.stream().collect( Collectors.toMap( Job::getJobName, Function.identity() ) );
        log.info( "JobTask initialized with {} registered jobs: {}", jobRegistry.size(), jobRegistry.keySet() );
    }

    /**
     * Main scheduling loop that runs every 30 seconds.
     * Checks for jobs that are due to run and delegates to the appropriate job implementation.
     * Exceptions from job execution are logged but do not stop the scheduler loop.
     */
    @Scheduled( fixedDelayString = "${scheduler.polling-interval-ms:30000}" )
    public void checkAndExecuteJobs()
    {
        log.debug( "JobTask checking for jobs due to run" );
        final List<JobConfig> jobsDue = jobService.findJobsDueToRun();
        if ( jobsDue.isEmpty() )
        {
            log.debug( "No jobs due to run at this time" );
            return;
        }

        log.info( "Found {} job(s) due to run", jobsDue.size() );
        for ( final JobConfig jobConfig : jobsDue )
        {
            try
            {
                executeJob( jobConfig );
            }
            catch ( final JobExecutionException e )
            {
                log.error( "Job execution failed with JobExecutionException: {}", e.getMessage(), e );
            }
            catch ( final Exception e )
            {
                log.error( "Job execution failed with unexpected exception: {}", e.getMessage(), e );
            }
        }
    }

    /**
     * Executes a single job if it can be claimed.
     *
     * @param jobConfig the job configuration
     * @throws JobExecutionException if job execution fails
     */
    private void executeJob( final JobConfig jobConfig )
    {
        final String jobName = jobConfig.getName();

        // Attempt to claim the job
        if ( !jobService.claimJob( jobName ) )
        {
            log.debug( "Job [{}] already claimed by another pod, skipping", jobName );
            return;
        }

        final var execution = jobService.startExecution( jobName ).orElse( null );

        // Find the job implementation
        final Job job = jobRegistry.get( jobName );
        if ( job == null )
        {
            log.error( "No job implementation found for job name [{}]", jobName );
            jobService.markJobFailed( jobName,
                new JobExecutionException( "No job implementation found for job: " + jobName ), execution );
            return;
        }

        // Execute the job
        log.info( "Executing job [{}]", jobName );
        try
        {
            job.execute();
            job.onSuccess();
            jobService.markJobCompleted( jobName, execution );
        }
        catch ( final Exception executionError )
        {
            log.error( "Job [{}] execution failed with error: {}", jobName, executionError.getMessage(),
                executionError );

            try
            {
                job.onFailure( executionError );
            }
            catch ( final Exception callbackError )
            {
                log.error( "Job [{}] onFailure callback failed: {}", jobName, callbackError.getMessage(),
                    callbackError );
            }
            jobService.markJobFailed( jobName, executionError, execution );
        }
    }
}
