package com.spring.scheduler.repository.job;

import com.spring.scheduler.common.job.JobStatus;
import com.spring.scheduler.domain.job.JobConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    /**
     * Finds jobs eligible for execution at the provided time.
     *
     * @param now current timestamp
     * @param idleStatus the expected idle status value
     * @return jobs due to run
     */
    @Query( """
        SELECT j
        FROM JobConfig j
        WHERE j.enabled = true
          AND j.status = :idleStatus
          AND j.nextRunTime <= :now
        """ )
    List<JobConfig> findJobsDueToRun( @Param( "now" ) LocalDateTime now, @Param( "idleStatus" ) JobStatus idleStatus );

    /**
     * Atomically claims a job for execution.
     *
     * @param name          the job name
     * @param now           current timestamp
     * @param idleStatus    the expected idle status value
     * @param runningStatus the status value to set when claiming the job
     * @return number of rows updated
     */
    @Modifying
    @Query( """
        UPDATE JobConfig j
        SET j.status = :runningStatus,
            j.lastRunTime = :now
        WHERE j.name = :name
          AND j.enabled = true
          AND j.status = :idleStatus
          AND j.nextRunTime <= :now
        """ )
    int claimJob( @Param( "name" ) String name, @Param( "now" ) LocalDateTime now,
        @Param( "idleStatus" ) JobStatus idleStatus, @Param( "runningStatus" ) JobStatus runningStatus );
}
