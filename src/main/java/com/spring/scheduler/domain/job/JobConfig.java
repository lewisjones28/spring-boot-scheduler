package com.spring.scheduler.domain.job;

import com.spring.scheduler.common.job.JobStatus;
import com.spring.scheduler.domain.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing scheduled job configuration.
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
@Table( name = "job_configs" )
public class JobConfig extends Auditable
{
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    private Integer id;

    @EqualsAndHashCode.Include
    @Column( nullable = false, unique = true )
    private String name;

    @Column
    private String description;

    @Enumerated( EnumType.STRING )
    @Column( nullable = false )
    private JobStatus status = JobStatus.IDLE;

    @Column( nullable = false )
    private LocalDateTime nextRunTime;

    @Column
    private LocalDateTime lastRunTime;

    @Column( nullable = false )
    private Long intervalMillis;

    @Column( nullable = false )
    private boolean enabled = false;

    /**
     * Constructor for creating a new scheduled job configuration.
     *
     * @param jobName        the unique name of the job
     * @param description    a brief description of the job's purpose
     * @param intervalMillis the interval between executions in milliseconds
     * @param nextRunTime    the next scheduled run time
     */
    public JobConfig( final String jobName, final String description, final Long intervalMillis,
        final LocalDateTime nextRunTime )
    {
        this.name = jobName;
        this.description = description;
        this.intervalMillis = intervalMillis;
        this.nextRunTime = nextRunTime;
    }

}

