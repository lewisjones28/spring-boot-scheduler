package com.spring.scheduler.repository.job;

import com.spring.scheduler.domain.job.JobExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing {@link JobExecution} entities.
 *
 * @author lewisjones
 */
@Repository
public interface JobExecutionRepository extends JpaRepository<JobExecution, Long>
{
}
