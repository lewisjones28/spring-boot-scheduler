package com.spring.scheduler.service.job;

import com.spring.scheduler.domain.job.JobConfig;
import com.spring.scheduler.domain.job.JobExecution;
import com.spring.scheduler.repository.job.JobExecutionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link JobExecutionService} unit tests.
 *
 * @author lewisjones
 */
@ExtendWith( MockitoExtension.class )
class JobExecutionServiceTest
{
    private static final Long EXECUTION_ID = 100L;
    private static final Long DEFAULT_INTERVAL_MILLIS = 60000L;

    @Mock
    private JobExecutionRepository jobExecutionRepository;

    @InjectMocks
    private JobExecutionService jobExecutionService;

    /**
     * Provides both present and empty findById repository outcomes.
     *
     * @return argument stream for findById parameterized test
     */
    private static Stream<Arguments> provideFindByIdResults()
    {
        final JobExecution existingExecution = createExecution( "hourlySync", "pod-2" );
        existingExecution.setId( EXECUTION_ID );
        return Stream.of( Arguments.of( Optional.of( existingExecution ) ), Arguments.of( Optional.empty() ) );
    }

    /**
     * Creates a valid {@link JobExecution} instance for test scenarios.
     *
     * @param jobName    job name
     * @param executedBy execution node identifier
     * @return populated execution
     */
    private static JobExecution createExecution( final String jobName, final String executedBy )
    {
        final JobConfig config =
            new JobConfig( jobName, "Test description", DEFAULT_INTERVAL_MILLIS, LocalDateTime.now().plusMinutes( 1 ) );
        return new JobExecution( config, LocalDateTime.now(), executedBy );
    }

    /**
     * Tests save when repository persists successfully.
     */
    @Test
    void testSaveWithValidExecutionReturnsSavedEntity()
    {
        // Given
        final JobExecution execution = createExecution( "dailyCleanup", "pod-1" );
        final JobExecution savedExecution = createExecution( "dailyCleanup", "pod-1" );
        savedExecution.setId( EXECUTION_ID );
        when( jobExecutionRepository.save( execution ) ).thenReturn( savedExecution );

        // When
        final JobExecution result = jobExecutionService.save( execution );

        // Then
        assertSame( savedExecution, result );
        verify( jobExecutionRepository ).save( execution );
        verifyNoMoreInteractions( jobExecutionRepository );
    }

    /**
     * Tests save when repository throws an exception.
     */
    @Test
    void testSaveWithRepositoryFailureThrowsException()
    {
        // Given
        final JobExecution execution = createExecution( "dailyCleanup", "pod-1" );
        final RuntimeException expected = new RuntimeException( "save failed" );
        when( jobExecutionRepository.save( execution ) ).thenThrow( expected );

        // When
        final RuntimeException actual =
            assertThrows( RuntimeException.class, () -> jobExecutionService.save( execution ) );

        // Then
        assertSame( expected, actual );
        verify( jobExecutionRepository ).save( execution );
        verifyNoMoreInteractions( jobExecutionRepository );
    }

    /**
     * Tests findById returns repository result for both present and empty outcomes.
     *
     * @param repositoryResult repository lookup result
     */
    @ParameterizedTest
    @MethodSource( "provideFindByIdResults" )
    void testFindByIdWithExistingAndMissingResultsReturnsRepositoryValue(
        final Optional<JobExecution> repositoryResult )
    {
        // Given
        when( jobExecutionRepository.findById( EXECUTION_ID ) ).thenReturn( repositoryResult );

        // When
        final Optional<JobExecution> result = jobExecutionService.findById( EXECUTION_ID );

        // Then
        assertEquals( repositoryResult, result );
        verify( jobExecutionRepository ).findById( EXECUTION_ID );
        verifyNoMoreInteractions( jobExecutionRepository );
    }

    /**
     * Tests findById when repository throws an exception.
     */
    @Test
    void testFindByIdWithRepositoryFailureThrowsException()
    {
        // Given
        final RuntimeException expected = new RuntimeException( "lookup failed" );
        when( jobExecutionRepository.findById( EXECUTION_ID ) ).thenThrow( expected );

        // When
        final RuntimeException actual =
            assertThrows( RuntimeException.class, () -> jobExecutionService.findById( EXECUTION_ID ) );

        // Then
        assertSame( expected, actual );
        verify( jobExecutionRepository ).findById( EXECUTION_ID );
        verifyNoMoreInteractions( jobExecutionRepository );
    }
}
