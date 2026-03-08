package com.spring.scheduler.domain.job;

import com.spring.scheduler.common.job.JobExecutionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a scheduled job execution record.
 *
 * @author lewisjones
 */
@Getter
@Setter
@EqualsAndHashCode( onlyExplicitlyIncluded = true )
@NoArgsConstructor( access = AccessLevel.PROTECTED )
@AllArgsConstructor
@Entity
@Access( AccessType.FIELD )
@Table( name = "job_executions" )
public class JobExecution
{
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    private Long id;

    @EqualsAndHashCode.Include
    @ManyToOne( fetch = FetchType.LAZY, optional = false )
    @JoinColumn( name = "job_id", nullable = false )
    private JobConfig job;

    @Enumerated( EnumType.STRING )
    @Column( nullable = false )
    private JobExecutionStatus status;

    @EqualsAndHashCode.Include
    @Column( name = "started_at", nullable = false )
    private LocalDateTime startedAt;

    @Column( name = "completed_at" )
    private LocalDateTime completedAt;

    @Column( name = "error_message", columnDefinition = "TEXT" )
    private String errorMessage;

    @Column( name = "error_stacktrace", columnDefinition = "TEXT" )
    private String errorStacktrace;

    @Column( name = "executed_by" )
    private String executedBy;

    /**
     * Creates a new execution record for a scheduled job.
     *
     * @param job        the scheduled job configuration
     * @param startedAt  when the execution started
     * @param executedBy the pod or host identifier
     */
    public JobExecution( final JobConfig job, final LocalDateTime startedAt, final String executedBy )
    {
        this.job = job;
        this.startedAt = startedAt;
        this.executedBy = executedBy;
        this.status = JobExecutionStatus.RUNNING;
    }

}
