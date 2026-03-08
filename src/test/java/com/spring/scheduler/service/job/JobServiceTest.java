package com.spring.scheduler.service.job;

import com.spring.scheduler.common.job.JobExecutionStatus;
import com.spring.scheduler.common.job.JobStatus;
import com.spring.scheduler.domain.job.JobConfig;
import com.spring.scheduler.domain.job.JobExecution;
import com.spring.scheduler.exception.job.JobExecutionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link JobService} unit tests.
 *
 * @author lewisjones
 */
@ExtendWith( MockitoExtension.class )
class JobServiceTest
{
    private static final Long DEFAULT_INTERVAL_MILLIS = 60000L;
    private static final Long EXECUTION_ID = 10L;

    @Mock
    private JobConfigService jobConfigService;

    @Mock
    private JobExecutionService jobExecutionService;

    @InjectMocks
    private JobService jobService;

    /**
     * Provides claim outcomes for parameterized claimJob tests.
     *
     * @return claim result argument stream
     */
    private static Stream<Arguments> provideClaimResults()
    {
        return Stream.of( Arguments.of( Boolean.TRUE ), Arguments.of( Boolean.FALSE ) );
    }

    /**
     * Creates a valid {@link JobConfig} test fixture.
     *
     * @param name job name
     * @return populated job config
     */
    private static JobConfig createJobConfig( final String name )
    {
        return new JobConfig( name, "test job", DEFAULT_INTERVAL_MILLIS, LocalDateTime.now().plusMinutes( 1 ) );
    }

    /**
     * Creates a running {@link JobExecution} test fixture.
     *
     * @param config job config
     * @return execution record
     */
    private static JobExecution createExecution( final JobConfig config )
    {
        final JobExecution execution = new JobExecution( config, LocalDateTime.now().minusSeconds( 5 ), "test-pod" );
        execution.setStatus( JobExecutionStatus.RUNNING );
        execution.setCompletedAt( null );
        execution.setErrorMessage( null );
        execution.setErrorStacktrace( null );
        return execution;
    }

    /**
     * Tests findJobsDueToRun delegates to JobConfigService.
     */
    @Test
    void testFindJobsDueToRunWithDueJobsReturnsServiceResult()
    {
        // Given
        final List<JobConfig> expected = List.of( createJobConfig( "jobA" ), createJobConfig( "jobB" ) );
        when( jobConfigService.findJobsDueToRun() ).thenReturn( expected );

        // When
        final List<JobConfig> result = jobService.findJobsDueToRun();

        // Then
        assertSame( expected, result );
        verify( jobConfigService ).findJobsDueToRun();
        verifyNoMoreInteractions( jobConfigService, jobExecutionService );
    }

    /**
     * Tests claimJob delegates result from JobConfigService.
     *
     * @param claimResult expected claim result
     */
    @ParameterizedTest
    @MethodSource( "provideClaimResults" )
    void testClaimJobWithClaimOutcomesReturnsDelegatedResult( final boolean claimResult )
    {
        // Given
        final String jobName = "dailyCleanup";
        when( jobConfigService.claimJob( jobName ) ).thenReturn( claimResult );

        // When
        final boolean result = jobService.claimJob( jobName );

        // Then
        assertEquals( claimResult, result );
        verify( jobConfigService ).claimJob( jobName );
        verifyNoMoreInteractions( jobConfigService, jobExecutionService );
    }

    /**
     * Tests startExecution when job exists creates and saves execution record.
     */
    @Test
    void testStartExecutionWithExistingJobCreatesAndSavesExecution()
    {
        // Given
        final String jobName = "dailyCleanup";
        final JobConfig config = createJobConfig( jobName );
        final JobExecution saved = new JobExecution( config, LocalDateTime.now(), "test-pod" );
        saved.setId( EXECUTION_ID );

        when( jobConfigService.findByName( jobName ) ).thenReturn( Optional.of( config ) );
        when( jobExecutionService.save( any( JobExecution.class ) ) ).thenReturn( saved );

        // When
        final Optional<JobExecution> result = jobService.startExecution( jobName );

        // Then
        assertTrue( result.isPresent() );
        assertSame( saved, result.get() );
        verify( jobConfigService ).findByName( jobName );
        verify( jobExecutionService ).save( any( JobExecution.class ) );
        verifyNoMoreInteractions( jobConfigService, jobExecutionService );
    }

    /**
     * Tests startExecution when job is missing returns empty and does not save execution.
     */
    @Test
    void testStartExecutionWithMissingJobReturnsEmpty()
    {
        // Given
        final String jobName = "missingJob";
        when( jobConfigService.findByName( jobName ) ).thenReturn( Optional.empty() );

        // When
        final Optional<JobExecution> result = jobService.startExecution( jobName );

        // Then
        assertTrue( result.isEmpty() );
        verify( jobConfigService ).findByName( jobName );
        verify( jobExecutionService, never() ).save( any( JobExecution.class ) );
        verifyNoMoreInteractions( jobConfigService, jobExecutionService );
    }

    /**
     * Tests markJobCompleted updates job state and execution state when both exist.
     */
    @Test
    void testMarkJobCompletedWithExecutionUpdatesConfigAndExecution()
    {
        // Given
        final String jobName = "dailyCleanup";
        final JobConfig config = createJobConfig( jobName );
        final JobExecution execution = createExecution( config );
        when( jobConfigService.findByName( jobName ) ).thenReturn( Optional.of( config ) );

        // When
        jobService.markJobCompleted( jobName, execution );

        // Then
        assertEquals( JobStatus.IDLE, config.getStatus() );
        assertNotNull( config.getNextRunTime() );
        assertEquals( JobExecutionStatus.SUCCESS, execution.getStatus() );
        assertNotNull( execution.getCompletedAt() );
        verify( jobConfigService ).findByName( jobName );
        verify( jobConfigService ).save( config );
        verify( jobExecutionService ).save( execution );
        verifyNoMoreInteractions( jobConfigService, jobExecutionService );
    }

    /**
     * Tests markJobCompleted when execution is null updates config only.
     */
    @Test
    void testMarkJobCompletedWithNullExecutionUpdatesConfigOnly()
    {
        // Given
        final String jobName = "dailyCleanup";
        final JobConfig config = createJobConfig( jobName );
        when( jobConfigService.findByName( jobName ) ).thenReturn( Optional.of( config ) );

        // When
        jobService.markJobCompleted( jobName, null );

        // Then
        assertEquals( JobStatus.IDLE, config.getStatus() );
        verify( jobConfigService ).findByName( jobName );
        verify( jobConfigService ).save( config );
        verify( jobExecutionService, never() ).save( any( JobExecution.class ) );
        verifyNoMoreInteractions( jobConfigService, jobExecutionService );
    }

    /**
     * Tests markJobCompleted when job is missing performs no updates.
     */
    @Test
    void testMarkJobCompletedWithMissingJobDoesNothing()
    {
        // Given
        final String jobName = "missingJob";
        final JobExecution execution = createExecution( createJobConfig( "unused" ) );
        when( jobConfigService.findByName( jobName ) ).thenReturn( Optional.empty() );

        // When
        jobService.markJobCompleted( jobName, execution );

        // Then
        verify( jobConfigService ).findByName( jobName );
        verify( jobConfigService, never() ).save( any( JobConfig.class ) );
        verify( jobExecutionService, never() ).save( any( JobExecution.class ) );
        verifyNoMoreInteractions( jobConfigService, jobExecutionService );
    }

    /**
     * Tests markJobFailed updates both config and execution details.
     */
    @Test
    void testMarkJobFailedWithExecutionUpdatesFailureState()
    {
        // Given
        final String jobName = "failingJob";
        final JobConfig config = createJobConfig( jobName );
        final JobExecution execution = createExecution( config );
        final RuntimeException error = new RuntimeException( "boom" );
        when( jobConfigService.findByName( jobName ) ).thenReturn( Optional.of( config ) );

        // When
        jobService.markJobFailed( jobName, error, execution );

        // Then
        assertEquals( JobStatus.IDLE, config.getStatus() );
        assertNotNull( config.getNextRunTime() );
        assertEquals( JobExecutionStatus.FAILED, execution.getStatus() );
        assertNotNull( execution.getCompletedAt() );
        assertEquals( "boom", execution.getErrorMessage() );
        assertNotNull( execution.getErrorStacktrace() );
        verify( jobConfigService ).findByName( jobName );
        verify( jobConfigService ).save( config );
        verify( jobExecutionService ).save( execution );
        verifyNoMoreInteractions( jobConfigService, jobExecutionService );
    }

    /**
     * Tests markJobFailed with null execution updates config only.
     */
    @Test
    void testMarkJobFailedWithNullExecutionUpdatesConfigOnly()
    {
        // Given
        final String jobName = "failingJob";
        final JobConfig config = createJobConfig( jobName );
        final RuntimeException error = new RuntimeException( "boom" );
        when( jobConfigService.findByName( jobName ) ).thenReturn( Optional.of( config ) );

        // When
        jobService.markJobFailed( jobName, error, null );

        // Then
        assertEquals( JobStatus.IDLE, config.getStatus() );
        verify( jobConfigService ).findByName( jobName );
        verify( jobConfigService ).save( config );
        verify( jobExecutionService, never() ).save( any( JobExecution.class ) );
        verifyNoMoreInteractions( jobConfigService, jobExecutionService );
    }

    /**
     * Tests markJobFailed throws when job config is missing.
     */
    @Test
    void testMarkJobFailedWithMissingJobThrowsJobExecutionException()
    {
        // Given
        final String jobName = "missingJob";
        final RuntimeException error = new RuntimeException( "boom" );
        when( jobConfigService.findByName( jobName ) ).thenReturn( Optional.empty() );

        // When
        final JobExecutionException thrown =
            assertThrows( JobExecutionException.class, () -> jobService.markJobFailed( jobName, error, null ) );

        // Then
        assertEquals( "Job [missingJob] not found in configuration", thrown.getMessage() );
        verify( jobConfigService ).findByName( jobName );
        verify( jobConfigService, never() ).save( any( JobConfig.class ) );
        verify( jobExecutionService, never() ).save( any( JobExecution.class ) );
        verifyNoMoreInteractions( jobConfigService, jobExecutionService );
    }

    /**
     * Tests save delegates to JobConfigService.
     */
    @Test
    void testSaveWithValidConfigDelegatesAndReturnsSaved()
    {
        // Given
        final JobConfig config = createJobConfig( "saveJob" );
        when( jobConfigService.save( config ) ).thenReturn( config );

        // When
        final JobConfig result = jobService.save( config );

        // Then
        assertSame( config, result );
        verify( jobConfigService ).save( config );
        verifyNoMoreInteractions( jobConfigService, jobExecutionService );
    }
}
