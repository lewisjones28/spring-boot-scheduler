package com.spring.scheduler.startup;

import com.spring.scheduler.job.Job;
import com.spring.scheduler.service.job.JobConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Startup task that discovers and persists {@link Job} implementations into the `job_configs` table.
 *
 * @author lewisjones
 */
@Component
@Slf4j
public class JobStartup implements ApplicationListener<ApplicationReadyEvent>
{
    private final List<Job> jobs;
    private final JobConfigService jobConfigService;
    @Value( "${scheduler.interval-ms:86400000}" )
    private Long defaultIntervalMillis;

    /**
     * Constructs a new startup job registrar.
     *
     * @param jobs             the list of schedulable jobs
     * @param jobConfigService service for scheduled job configs
     */
    public JobStartup( final List<Job> jobs, final JobConfigService jobConfigService )
    {
        this.jobs = jobs;
        this.jobConfigService = jobConfigService;
    }

    /**
     * Handles the {@link ApplicationReadyEvent} to register all {@link Job} implementations.
     *
     * @param event the application ready event
     */
    @Override
    @Transactional
    public void onApplicationEvent( final ApplicationReadyEvent event )
    {
        log.info( "Starting scheduled job registration process..." );
        final long registeredCount = jobs.stream().map( job ->
        {
            final String jobName = job.getJobName();
            log.debug( "Processing scheduled job: {}", jobName );

            final boolean created =
                jobConfigService.registerIfMissing( jobName, job.getJobDescription(), defaultIntervalMillis,
                    LocalDateTime.now() );

            if ( !created )
            {
                log.debug( "Scheduled job already exists: {}", jobName );
                return Boolean.FALSE;
            }

            log.info( "Registered new scheduled job: {} with default interval: {} ms", jobName, defaultIntervalMillis );
            return Boolean.TRUE;
        } ).filter( Boolean::booleanValue ).count();

        final long existingCount = jobs.size() - registeredCount;
        log.info( "Scheduled job registration completed. Registered: {}, Already existing: {}, Total jobs found: {}",
            registeredCount, existingCount, jobs.size() );
    }
}
