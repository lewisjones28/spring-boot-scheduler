package com.spring.scheduler.domain.job;

import com.spring.scheduler.common.job.JobExecutionStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link JobExecution} unit tests.
 *
 * @author lewisjones
 */
class JobExecutionTest
{

    private static final Long DEFAULT_INTERVAL_MILLIS = 3600000L;
    private static final Long DIFFERENT_EXECUTION_ID = 999L;

    /**
     * Test equals with same job and startedAt returns true.
     */
    @Test
    void testEqualsWithSameJobAndStartedAt()
    {
        // Given
        final JobConfig job = createJobConfig( "daily-cleanup" );
        final LocalDateTime startedAt = LocalDateTime.now();
        final JobExecution execution1 = createJobExecution( job, startedAt );
        final JobExecution execution2 = createJobExecution( job, startedAt );

        // When
        final boolean result = execution1.equals( execution2 );

        // Then
        assertTrue( result, "JobExecutions with same job and startedAt should be equal" );
    }

    /**
     * Test equals with different jobs returns false.
     */
    @Test
    void testEqualsWithDifferentJobs()
    {
        // Given
        final JobConfig job1 = createJobConfig( "daily-cleanup" );
        final JobConfig job2 = createJobConfig( "hourly-sync" );
        final LocalDateTime startedAt = LocalDateTime.now();
        final JobExecution execution1 = createJobExecution( job1, startedAt );
        final JobExecution execution2 = createJobExecution( job2, startedAt );

        // When
        final boolean result = execution1.equals( execution2 );

        // Then
        assertFalse( result, "JobExecutions with different jobs should not be equal" );
    }

    /**
     * Test equals with different startedAt returns false.
     */
    @Test
    void testEqualsWithDifferentStartedAt()
    {
        // Given
        final JobConfig job = createJobConfig( "daily-cleanup" );
        final LocalDateTime startedAt1 = LocalDateTime.now();
        final LocalDateTime startedAt2 = startedAt1.plusMinutes( 5 );
        final JobExecution execution1 = createJobExecution( job, startedAt1 );
        final JobExecution execution2 = createJobExecution( job, startedAt2 );

        // When
        final boolean result = execution1.equals( execution2 );

        // Then
        assertFalse( result, "JobExecutions with different startedAt should not be equal" );
    }

    /**
     * Test equals with same instance returns true.
     */
    @Test
    void testEqualsWithSameInstance()
    {
        // Given
        final JobExecution execution = createJobExecution( createJobConfig( "daily-cleanup" ), LocalDateTime.now() );

        // When
        final boolean result = execution.equals( execution );

        // Then
        assertTrue( result, "JobExecution should equal itself" );
    }

    /**
     * Test equals with null returns false.
     */
    @Test
    void testEqualsWithNull()
    {
        // Given
        final JobExecution execution = createJobExecution( createJobConfig( "daily-cleanup" ), LocalDateTime.now() );

        // When
        final boolean result = execution.equals( null );

        // Then
        assertFalse( result, "JobExecution should not equal null" );
    }

    /**
     * Test equals with different class returns false.
     */
    @Test
    void testEqualsWithDifferentClass()
    {
        // Given
        final JobExecution execution = createJobExecution( createJobConfig( "daily-cleanup" ), LocalDateTime.now() );
        final String differentClassObject = "Not a JobExecution";

        // When
        final boolean result = execution.equals( differentClassObject );

        // Then
        assertFalse( result, "JobExecution should not equal object of different class" );
    }

    /**
     * Test hashCode consistency for same job and startedAt.
     */
    @Test
    void testHashCodeWithSameJobAndStartedAt()
    {
        // Given
        final JobConfig job = createJobConfig( "daily-cleanup" );
        final LocalDateTime startedAt = LocalDateTime.now();
        final JobExecution execution1 = createJobExecution( job, startedAt );
        final JobExecution execution2 = createJobExecution( job, startedAt );

        // When
        final int hash1 = execution1.hashCode();
        final int hash2 = execution2.hashCode();

        // Then
        assertEquals( hash1, hash2, "JobExecutions with same job and startedAt should have same hashCode" );
    }

    /**
     * Test hashCode difference for different jobs.
     */
    @Test
    void testHashCodeWithDifferentJobs()
    {
        // Given
        final JobConfig job1 = createJobConfig( "daily-cleanup" );
        final JobConfig job2 = createJobConfig( "hourly-sync" );
        final LocalDateTime startedAt = LocalDateTime.now();
        final JobExecution execution1 = createJobExecution( job1, startedAt );
        final JobExecution execution2 = createJobExecution( job2, startedAt );

        // When
        final int hash1 = execution1.hashCode();
        final int hash2 = execution2.hashCode();

        // Then
        assertNotEquals( hash1, hash2, "JobExecutions with different jobs should have different hashCodes" );
    }

    /**
     * Test hashCode difference for different startedAt.
     */
    @Test
    void testHashCodeWithDifferentStartedAt()
    {
        // Given
        final JobConfig job = createJobConfig( "daily-cleanup" );
        final LocalDateTime startedAt1 = LocalDateTime.now();
        final LocalDateTime startedAt2 = startedAt1.plusMinutes( 5 );
        final JobExecution execution1 = createJobExecution( job, startedAt1 );
        final JobExecution execution2 = createJobExecution( job, startedAt2 );

        // When
        final int hash1 = execution1.hashCode();
        final int hash2 = execution2.hashCode();

        // Then
        assertNotEquals( hash1, hash2, "JobExecutions with different startedAt should have different hashCodes" );
    }

    /**
     * Test hashCode consistency across multiple calls.
     */
    @Test
    void testHashCodeConsistency()
    {
        // Given
        final JobExecution execution = createJobExecution( createJobConfig( "daily-cleanup" ), LocalDateTime.now() );

        // When
        final int hash1 = execution.hashCode();
        final int hash2 = execution.hashCode();
        final int hash3 = execution.hashCode();

        // Then
        assertEquals( hash1, hash2, "hashCode should be consistent" );
        assertEquals( hash2, hash3, "hashCode should be consistent across multiple calls" );
    }

    /**
     * Test equals ignores non-business key fields.
     *
     * @param execution1 the first JobExecution instance
     * @param execution2 the second JobExecution instance
     */
    @ParameterizedTest
    @MethodSource( "provideJobExecutionsWithDifferentNonKeyFields" )
    void testEqualsIgnoresNonBusinessKeyFields( final JobExecution execution1, final JobExecution execution2 )
    {
        // When
        final boolean result = execution1.equals( execution2 );

        // Then
        assertTrue( result, "JobExecutions with same job and startedAt but different non-key fields should be equal" );
    }

    /**
     * Test constructor with minimal parameters.
     */
    @Test
    void testConstructorWithMinimalParameters()
    {
        // Given
        final JobConfig job = createJobConfig( "daily-cleanup" );
        final LocalDateTime startedAt = LocalDateTime.now();
        final String executedBy = "pod-123";

        // When
        final JobExecution execution = new JobExecution( job, startedAt, executedBy );

        // Then
        assertNotNull( execution, "JobExecution should be created" );
        assertEquals( job, execution.getJob(), "Job should match" );
        assertEquals( startedAt, execution.getStartedAt(), "StartedAt should match" );
        assertEquals( executedBy, execution.getExecutedBy(), "ExecutedBy should match" );
        assertEquals( JobExecutionStatus.RUNNING, execution.getStatus(), "Default status should be RUNNING" );
        assertNull( execution.getCompletedAt(), "CompletedAt should be null initially" );
        assertNull( execution.getErrorMessage(), "ErrorMessage should be null initially" );
        assertNull( execution.getErrorStacktrace(), "ErrorStacktrace should be null initially" );
    }

    /**
     * Test getters and setters.
     */
    @Test
    void testGettersAndSetters()
    {
        // Given
        final JobExecution execution = createJobExecution( createJobConfig( "test-job" ), LocalDateTime.now() );
        final Long id = 456L;
        final JobConfig newJob = createJobConfig( "new-job" );
        final JobExecutionStatus newStatus = JobExecutionStatus.SUCCESS;
        final LocalDateTime newStartedAt = LocalDateTime.now().minusHours( 1 );
        final LocalDateTime newCompletedAt = LocalDateTime.now();
        final String errorMessage = "Test error";
        final String errorStacktrace = "Stack trace here";
        final String executedBy = "pod-456";

        // When
        execution.setId( id );
        execution.setJob( newJob );
        execution.setStatus( newStatus );
        execution.setStartedAt( newStartedAt );
        execution.setCompletedAt( newCompletedAt );
        execution.setErrorMessage( errorMessage );
        execution.setErrorStacktrace( errorStacktrace );
        execution.setExecutedBy( executedBy );

        // Then
        assertEquals( id, execution.getId(), "ID should be set" );
        assertEquals( newJob, execution.getJob(), "Job should be updated" );
        assertEquals( newStatus, execution.getStatus(), "Status should be updated" );
        assertEquals( newStartedAt, execution.getStartedAt(), "StartedAt should be updated" );
        assertEquals( newCompletedAt, execution.getCompletedAt(), "CompletedAt should be updated" );
        assertEquals( errorMessage, execution.getErrorMessage(), "ErrorMessage should be updated" );
        assertEquals( errorStacktrace, execution.getErrorStacktrace(), "ErrorStacktrace should be updated" );
        assertEquals( executedBy, execution.getExecutedBy(), "ExecutedBy should be updated" );
    }

    /**
     * Test execution lifecycle - successful completion.
     */
    @Test
    void testExecutionLifecycleSuccessfulCompletion()
    {
        // Given
        final JobConfig job = createJobConfig( "daily-cleanup" );
        final LocalDateTime startedAt = LocalDateTime.now();
        final JobExecution execution = new JobExecution( job, startedAt, "pod-123" );

        // When - simulate successful execution
        execution.setStatus( JobExecutionStatus.SUCCESS );
        execution.setCompletedAt( LocalDateTime.now() );

        // Then
        assertEquals( JobExecutionStatus.SUCCESS, execution.getStatus(), "Status should be SUCCESS" );
        assertNotNull( execution.getCompletedAt(), "CompletedAt should be set" );
        assertNull( execution.getErrorMessage(), "ErrorMessage should remain null" );
        assertNull( execution.getErrorStacktrace(), "ErrorStacktrace should remain null" );
    }

    /**
     * Test execution lifecycle - failed with error.
     */
    @Test
    void testExecutionLifecycleFailedWithError()
    {
        // Given
        final JobConfig job = createJobConfig( "daily-cleanup" );
        final LocalDateTime startedAt = LocalDateTime.now();
        final JobExecution execution = new JobExecution( job, startedAt, "pod-123" );
        final String errorMessage = "Connection timeout";
        final String errorStacktrace = "java.sql.SQLException at...";

        // When - simulate failed execution
        execution.setStatus( JobExecutionStatus.FAILED );
        execution.setCompletedAt( LocalDateTime.now() );
        execution.setErrorMessage( errorMessage );
        execution.setErrorStacktrace( errorStacktrace );

        // Then
        assertEquals( JobExecutionStatus.FAILED, execution.getStatus(), "Status should be FAILED" );
        assertNotNull( execution.getCompletedAt(), "CompletedAt should be set" );
        assertEquals( errorMessage, execution.getErrorMessage(), "ErrorMessage should be set" );
        assertEquals( errorStacktrace, execution.getErrorStacktrace(), "ErrorStacktrace should be set" );
    }

    /**
     * Provides JobExecution instances with same job and startedAt but different non-key fields.
     *
     * @return stream of argument pairs containing JobExecutions with same business keys but different properties
     */
    private static Stream<Arguments> provideJobExecutionsWithDifferentNonKeyFields()
    {
        final JobConfig job = createJobConfig( "daily-cleanup" );
        final LocalDateTime startedAt = LocalDateTime.now();

        final JobExecution execution1 = createJobExecution( job, startedAt );
        execution1.setId( 1L );
        execution1.setStatus( JobExecutionStatus.RUNNING );

        final JobExecution execution2 = createJobExecution( job, startedAt );
        execution2.setId( DIFFERENT_EXECUTION_ID );
        execution2.setStatus( JobExecutionStatus.SUCCESS );
        execution2.setCompletedAt( LocalDateTime.now() );

        final JobExecution execution3 = createJobExecution( job, startedAt );
        execution3.setStatus( JobExecutionStatus.FAILED );
        execution3.setErrorMessage( "Test error" );
        execution3.setErrorStacktrace( "Stack trace" );
        execution3.setExecutedBy( "different-pod" );

        return Stream.of(
            Arguments.of( execution1, execution2 ),
            Arguments.of( execution1, execution3 ),
            Arguments.of( execution2, execution3 )
        );
    }

    /**
     * Helper method to create a JobConfig with minimal required fields.
     *
     * @param name the job name
     * @return a new JobConfig instance
     */
    private static JobConfig createJobConfig( final String name )
    {
        return new JobConfig(
            name,
            "Test job description",
            DEFAULT_INTERVAL_MILLIS,
            LocalDateTime.now().plusHours( 1 )
        );
    }

    /**
     * Helper method to create a JobExecution with minimal required fields.
     *
     * @param job       the job configuration
     * @param startedAt the start timestamp
     * @return a new JobExecution instance
     */
    private static JobExecution createJobExecution( final JobConfig job, final LocalDateTime startedAt )
    {
        return new JobExecution( job, startedAt, "pod-123" );
    }

}
