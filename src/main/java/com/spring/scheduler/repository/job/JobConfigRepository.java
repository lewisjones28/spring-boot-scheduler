package com.spring.scheduler.repository.job;

import java.util.Optional;

import com.spring.scheduler.domain.job.JobConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing {@link JobConfig} entities.
 *
 * @author lewisjones
 */
@Repository
public interface JobConfigRepository extends JpaRepository<JobConfig, Integer>
{
    /**
     * Finds a job configuration by job name.
     *
     * @param name the job name
     * @return the matching job configuration, if present
     */
    Optional<JobConfig> findByName( String name );
}
