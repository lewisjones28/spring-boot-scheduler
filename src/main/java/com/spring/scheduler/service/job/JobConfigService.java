package com.spring.scheduler.service.job;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.spring.scheduler.common.job.JobStatus;
import com.spring.scheduler.domain.job.JobConfig;
import com.spring.scheduler.repository.job.JobConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer for {@link JobConfig} persistence operations.
 *
 * @author lewisjones
 */
@Service
public class JobConfigService
{
    private final JobConfigRepository jobConfigRepository;

    /**
     * Constructs a new {@link JobConfigService}.
     *
     * @param jobConfigRepository repository for job config operations
     */
    public JobConfigService( final JobConfigRepository jobConfigRepository )
    {
        this.jobConfigRepository = jobConfigRepository;
    }

    /**
     * Registers a job configuration if one does not already exist for the provided name.
     *
     * @param name the job name
     * @param description the job description
     * @param intervalMillis the execution interval in milliseconds
     * @param nextRunTime the next scheduled run time
     * @return true if a new record was created, false if one already existed
     */
    @Transactional
    public boolean registerIfMissing( final String name, final String description,
        final Long intervalMillis, final LocalDateTime nextRunTime )
    {
        final var existing = findByName( name );
        if ( existing.isPresent() )
        {
            return Boolean.FALSE;
        }

        final JobConfig newJob = new JobConfig( name, description, intervalMillis, nextRunTime );
        save( newJob );
        return Boolean.TRUE;
    }

    /**
     * Finds a job configuration by name.
     *
     * @param name job name
     * @return matching configuration if present
     */
    @Transactional( readOnly = true )
    public Optional<JobConfig> findByName( final String name )
    {
        return jobConfigRepository.findByName( name );
    }

    /**
     * Finds jobs that are currently due for execution.
     *
     * @return due jobs
     */
    @Transactional( readOnly = true )
    public List<JobConfig> findJobsDueToRun()
    {
        return jobConfigRepository.findJobsDueToRun( LocalDateTime.now(), JobStatus.IDLE );
    }

    /**
     * Attempts to claim a due job for execution.
     *
     * @param jobName job name
     * @return true when claim succeeds
     */
    @Transactional
    public boolean claimJob( final String jobName )
    {
        final int updated =
            jobConfigRepository.claimJob( jobName, LocalDateTime.now(), JobStatus.IDLE, JobStatus.RUNNING );
        return updated > 0;
    }

    /**
     * Persists a job configuration.
     *
     * @param jobConfig configuration to save
     * @return saved entity
     */
    @Transactional
    public JobConfig save( final JobConfig jobConfig )
    {
        return jobConfigRepository.save( jobConfig );
    }
}
