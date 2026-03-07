package com.spring.scheduler.service.job;

import java.time.LocalDateTime;

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
        final var existing = jobConfigRepository.findByName( name );
        if ( existing.isPresent() )
        {
            return Boolean.FALSE;
        }

        final JobConfig newJob = new JobConfig( name, description, intervalMillis, nextRunTime );
        jobConfigRepository.save( newJob );
        return Boolean.TRUE;
    }
}
