package com.spring.scheduler.task.job;

import com.spring.scheduler.domain.job.JobConfig;
import com.spring.scheduler.domain.job.JobExecution;
import com.spring.scheduler.exception.job.JobExecutionException;
import com.spring.scheduler.jobs.Job;
import com.spring.scheduler.service.job.JobService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link JobTask} unit tests.
 *
 * @author lewisjones
 */
@ExtendWith( MockitoExtension.class )
class JobTaskTest
{
    private static final Long DEFAULT_INTERVAL_MILLIS = 60000L;

    @Mock
    private JobService jobService;

    @Mock
    private Job jobOne;

    @Mock
    private Job jobTwo;

    /**
     * Provides callback outcomes for parameterized failure tests.
     *
     * @return argument stream for onFailure throwing behavior
     */
    private static Stream<Arguments> provideOnFailureOutcomes()
    {
        return Stream.of( Arguments.of( Boolean.FALSE ), Arguments.of( Boolean.TRUE ) );
    }

    /**
     * Creates a due {@link JobConfig} for scheduler tests.
     *
     * @param name job name
     * @return due job configuration
     */
    private static JobConfig createDueJobConfig( final String name )
    {
        return new JobConfig( name, "test job", DEFAULT_INTERVAL_MILLIS, LocalDateTime.now().minusMinutes( 1 ) );
    }

    /**
     * Creates a running {@link JobExecution} tied to the provided config.
     *
     * @param config job configuration
     * @return job execution record
     */
    private static JobExecution createExecution( final JobConfig config )
    {
        return new JobExecution( config, LocalDateTime.now().minusSeconds( 5 ), "test-pod" );
    }

    /**
     * Tests scheduler loop when no jobs are due.
     */
    @Test
    void testCheckAndExecuteJobsWithNoDueJobsSkipsExecutionFlow()
    {
        // Given
        final JobTask jobTask = createTask( List.of() );
        when( jobService.findJobsDueToRun() ).thenReturn( List.of() );

        // When
        jobTask.checkAndExecuteJobs();

        // Then
        verify( jobService ).findJobsDueToRun();
        verifyNoMoreInteractions( jobService );
    }

    /**
     * Tests scheduler loop when a due job cannot be claimed.
     */
    @Test
    void testCheckAndExecuteJobsWithClaimRejectedSkipsExecution()
    {
        // Given
        final String jobName = "dailyCleanup";
        final JobConfig dueJob = createDueJobConfig( jobName );
        final JobTask jobTask = createTask( List.of() );

        when( jobService.findJobsDueToRun() ).thenReturn( List.of( dueJob ) );
        when( jobService.claimJob( jobName ) ).thenReturn( Boolean.FALSE );

        // When
        jobTask.checkAndExecuteJobs();

        // Then
        verify( jobService ).findJobsDueToRun();
        verify( jobService ).claimJob( jobName );
        verify( jobService, never() ).startExecution( any() );
        verify( jobService, never() ).markJobCompleted( any(), any() );
        verify( jobService, never() ).markJobFailed( any(), any(), any() );
        verifyNoMoreInteractions( jobService );
    }

    /**
     * Tests scheduler loop successful execution path.
     */
    @Test
    void testCheckAndExecuteJobsWithSuccessfulExecutionMarksCompleted()
    {
        // Given
        final String jobName = "dailyCleanup";
        final JobConfig dueJob = createDueJobConfig( jobName );
        final JobExecution execution = createExecution( dueJob );
        when( jobOne.getJobName() ).thenReturn( jobName );
        final JobTask jobTask = createTask( List.of( jobOne ) );

        when( jobService.findJobsDueToRun() ).thenReturn( List.of( dueJob ) );
        when( jobService.claimJob( jobName ) ).thenReturn( Boolean.TRUE );
        when( jobService.startExecution( jobName ) ).thenReturn( Optional.of( execution ) );

        // When
        jobTask.checkAndExecuteJobs();

        // Then
        verify( jobOne ).getJobName();
        verify( jobService ).findJobsDueToRun();
        verify( jobService ).claimJob( jobName );
        verify( jobService ).startExecution( jobName );
        verify( jobOne ).execute();
        verify( jobOne ).onSuccess();
        verify( jobService ).markJobCompleted( jobName, execution );
        verify( jobService, never() ).markJobFailed( any(), any(), any() );
        verifyNoMoreInteractions( jobService, jobOne );
    }

    /**
     * Tests scheduler loop when no implementation exists for a configured job.
     */
    @Test
    void testCheckAndExecuteJobsWithMissingImplementationMarksFailed()
    {
        // Given
        final String configuredJobName = "unmappedJob";
        final JobConfig dueJob = createDueJobConfig( configuredJobName );
        final JobExecution execution = createExecution( dueJob );
        when( jobOne.getJobName() ).thenReturn( "differentJob" );
        final JobTask jobTask = createTask( List.of( jobOne ) );

        when( jobService.findJobsDueToRun() ).thenReturn( List.of( dueJob ) );
        when( jobService.claimJob( configuredJobName ) ).thenReturn( Boolean.TRUE );
        when( jobService.startExecution( configuredJobName ) ).thenReturn( Optional.of( execution ) );

        // When
        jobTask.checkAndExecuteJobs();

        // Then
        verify( jobOne ).getJobName();
        verify( jobService ).findJobsDueToRun();
        verify( jobService ).claimJob( configuredJobName );
        verify( jobService ).startExecution( configuredJobName );
        verify( jobService ).markJobFailed( eq( configuredJobName ), any( JobExecutionException.class ),
            eq( execution ) );
        verify( jobService, never() ).markJobCompleted( any(), any() );
        verifyNoMoreInteractions( jobService, jobOne );
    }

    /**
     * Tests execution failure handling with both callback outcomes.
     *
     * @param onFailureThrows whether job.onFailure throws
     */
    @ParameterizedTest
    @MethodSource( "provideOnFailureOutcomes" )
    void testCheckAndExecuteJobsWithExecutionFailureMarksFailed( final boolean onFailureThrows )
    {
        // Given
        final String jobName = "failingJob";
        final JobConfig dueJob = createDueJobConfig( jobName );
        final JobExecution execution = createExecution( dueJob );
        final RuntimeException executionError = new RuntimeException( "execution failed" );
        when( jobOne.getJobName() ).thenReturn( jobName );
        final JobTask jobTask = createTask( List.of( jobOne ) );

        when( jobService.findJobsDueToRun() ).thenReturn( List.of( dueJob ) );
        when( jobService.claimJob( jobName ) ).thenReturn( Boolean.TRUE );
        when( jobService.startExecution( jobName ) ).thenReturn( Optional.of( execution ) );
        doThrow( executionError ).when( jobOne ).execute();

        if ( onFailureThrows )
        {
            doThrow( new RuntimeException( "callback failed" ) ).when( jobOne ).onFailure( executionError );
        }

        // When
        jobTask.checkAndExecuteJobs();

        // Then
        verify( jobOne ).getJobName();
        verify( jobOne ).execute();
        verify( jobOne ).onFailure( executionError );
        verify( jobService ).markJobFailed( jobName, executionError, execution );
        verify( jobService, never() ).markJobCompleted( any(), any() );
        verifyNoMoreInteractions( jobService, jobOne );
    }

    /**
     * Tests that one failing job does not stop subsequent jobs in the same scheduler cycle.
     */
    @Test
    void testCheckAndExecuteJobsWithFirstJobFailureContinuesToSecondJob()
    {
        // Given
        final String failingJobName = "failingJob";
        final String successJobName = "successJob";
        final JobConfig failingConfig = createDueJobConfig( failingJobName );
        final JobConfig successConfig = createDueJobConfig( successJobName );
        final JobExecution failingExecution = createExecution( failingConfig );
        final JobExecution successExecution = createExecution( successConfig );

        when( jobOne.getJobName() ).thenReturn( failingJobName );
        when( jobTwo.getJobName() ).thenReturn( successJobName );
        final JobTask jobTask = createTask( List.of( jobOne, jobTwo ) );

        when( jobService.findJobsDueToRun() ).thenReturn( List.of( failingConfig, successConfig ) );
        when( jobService.claimJob( failingJobName ) ).thenReturn( Boolean.TRUE );
        when( jobService.startExecution( failingJobName ) ).thenReturn( Optional.of( failingExecution ) );
        doThrow( new RuntimeException( "boom" ) ).when( jobOne ).execute();
        doThrow( new JobExecutionException( "persist fail" ) ).when( jobService )
            .markJobFailed( eq( failingJobName ), any(), eq( failingExecution ) );

        when( jobService.claimJob( successJobName ) ).thenReturn( Boolean.TRUE );
        when( jobService.startExecution( successJobName ) ).thenReturn( Optional.of( successExecution ) );

        // When
        jobTask.checkAndExecuteJobs();

        // Then
        verify( jobOne ).getJobName();
        verify( jobTwo ).getJobName();
        verify( jobOne ).execute();
        verify( jobService ).markJobFailed( eq( failingJobName ), any(), eq( failingExecution ) );

        verify( jobTwo ).execute();
        verify( jobTwo ).onSuccess();
        verify( jobService ).markJobCompleted( successJobName, successExecution );
        verify( jobService, times( 2 ) ).claimJob( any() );
    }

    /**
     * Creates a {@link JobTask} instance with mocked collaborators.
     *
     * @param jobs registered jobs
     * @return configured task instance
     */
    private JobTask createTask( final List<Job> jobs )
    {
        return new JobTask( jobService, jobs );
    }
}
