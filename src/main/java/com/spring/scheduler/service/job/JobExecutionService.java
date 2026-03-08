package com.spring.scheduler.service.job;

import com.spring.scheduler.domain.job.JobExecution;
import com.spring.scheduler.repository.job.JobExecutionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service layer for {@link JobExecution} persistence operations.
 *
 * @author lewisjones
 */
@Service
public class JobExecutionService
{
    private final JobExecutionRepository jobExecutionRepository;

    /**
     * Constructs a new {@link JobExecutionService}.
     *
     * @param jobExecutionRepository repository for job execution operations
     */
    public JobExecutionService( final JobExecutionRepository jobExecutionRepository )
    {
        this.jobExecutionRepository = jobExecutionRepository;
    }

    /**
     * Persists a job execution record.
     *
     * @param jobExecution the execution to save
     * @return saved execution
     */
    @Transactional
    public JobExecution save( final JobExecution jobExecution )
    {
        return jobExecutionRepository.save( jobExecution );
    }

    /**
     * Finds a job execution by id.
     *
     * @param id execution id
     * @return matching execution, if present
     */
    @Transactional( readOnly = true )
    public Optional<JobExecution> findById( final Long id )
    {
        return jobExecutionRepository.findById( id );
    }
}
