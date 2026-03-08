package com.spring.scheduler.service.job;

import com.spring.scheduler.domain.job.JobConfig;
import com.spring.scheduler.jobs.Job;
import com.spring.scheduler.repository.job.JobConfigRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link JobConfigService} unit tests.
 *
 * @author lewisjones
 */
@ExtendWith( MockitoExtension.class )
class JobConfigServiceTest
{
    private static final Long DEFAULT_INTERVAL_MILLIS = 60000L;
    private static final Long HOURLY_INTERVAL_MILLIS = 300000L;
    private static final Long NIGHTLY_INTERVAL_MILLIS = 3600000L;

    @Mock
    private JobConfigRepository jobConfigRepository;

    @Mock
    private Job job;

    @InjectMocks
    private JobConfigService jobConfigService;

    /**
     * Provides valid registerIfMissing input combinations.
     *
     * @return input argument stream for parameterized test coverage
     */
    private static Stream<Arguments> provideRegisterInputs()
    {
        return Stream.of(
            Arguments.of( "dailyCleanup", "Daily cleanup", DEFAULT_INTERVAL_MILLIS ),
            Arguments.of( "hourlySync", "Hourly sync", HOURLY_INTERVAL_MILLIS ),
            Arguments.of( "nightlyRollup", "Nightly rollup", NIGHTLY_INTERVAL_MILLIS )
        );
    }

    /**
     * Tests registerIfMissing when config already exists.
     */
    @Test
    void testRegisterIfMissingWithExistingJobConfigReturnsFalse()
    {
        // Given
        final String name = "dailyCleanup";
        final String description = "Daily cleanup";
        final LocalDateTime nextRunTime = LocalDateTime.now();
        final JobConfig existing = new JobConfig( name, description, DEFAULT_INTERVAL_MILLIS, nextRunTime );

        when( job.getJobName() ).thenReturn( name );
        when( job.getJobDescription() ).thenReturn( description );
        when( job.getIntervalMs() ).thenReturn( 0L );
        when( jobConfigRepository.findByName( name ) ).thenReturn( Optional.of( existing ) );

        // When
        final boolean result = jobConfigService.registerIfMissing( job, DEFAULT_INTERVAL_MILLIS, nextRunTime );

        // Then
        assertFalse( result );
        verify( job ).getJobName();
        verify( job ).getJobDescription();
        verify( job ).getIntervalMs();
        verify( jobConfigRepository ).findByName( name );
        verify( jobConfigRepository, never() ).save( org.mockito.ArgumentMatchers.any( JobConfig.class ) );
        verifyNoMoreInteractions( jobConfigRepository, job );
    }

    /**
     * Tests registerIfMissing when config does not exist.
     */
    @Test
    void testRegisterIfMissingWithMissingJobConfigSavesAndReturnsTrue()
    {
        // Given
        final String name = "hourlySync";
        final String description = "Hourly synchronization";
        final Long jobInterval = HOURLY_INTERVAL_MILLIS;
        final LocalDateTime nextRunTime = LocalDateTime.now().plusMinutes( 5 );

        when( job.getJobName() ).thenReturn( name );
        when( job.getJobDescription() ).thenReturn( description );
        when( job.getIntervalMs() ).thenReturn( jobInterval );
        when( jobConfigRepository.findByName( name ) ).thenReturn( Optional.empty() );

        // When
        final boolean result = jobConfigService.registerIfMissing( job, DEFAULT_INTERVAL_MILLIS, nextRunTime );

        // Then
        assertTrue( result );
        final ArgumentCaptor<JobConfig> captor = ArgumentCaptor.forClass( JobConfig.class );
        verify( job ).getJobName();
        verify( job ).getJobDescription();
        verify( job, atLeast( 1 ) ).getIntervalMs();
        verify( jobConfigRepository ).findByName( name );
        verify( jobConfigRepository ).save( captor.capture() );
        verifyNoMoreInteractions( jobConfigRepository, job );

        final JobConfig saved = captor.getValue();
        assertEquals( name, saved.getName() );
        assertEquals( description, saved.getDescription() );
        assertEquals( jobInterval, saved.getIntervalMillis() );
        assertEquals( nextRunTime, saved.getNextRunTime() );
    }

    /**
     * Tests registerIfMissing uses job interval when provided, falls back to default when job returns 0.
     *
     * @param jobName the job name
     * @param jobDescription the job description
     * @param jobIntervalMs the job interval
     */
    @ParameterizedTest
    @MethodSource( "provideRegisterInputs" )
    void testRegisterIfMissingWithJobIntervalMapsFieldsCorrectly( final String jobName, final String jobDescription,
        final Long jobIntervalMs )
    {
        // Given
        final LocalDateTime nextRunTime = LocalDateTime.now().plusMinutes( 1 );

        when( job.getJobName() ).thenReturn( jobName );
        when( job.getJobDescription() ).thenReturn( jobDescription );
        when( job.getIntervalMs() ).thenReturn( jobIntervalMs );
        when( jobConfigRepository.findByName( jobName ) ).thenReturn( Optional.empty() );

        // When
        final boolean result = jobConfigService.registerIfMissing( job, DEFAULT_INTERVAL_MILLIS, nextRunTime );

        // Then
        assertTrue( result );
        final ArgumentCaptor<JobConfig> captor = ArgumentCaptor.forClass( JobConfig.class );
        verify( job ).getJobName();
        verify( job, atLeast( 1 ) ).getJobDescription();
        verify( job, atLeast( 1 ) ).getIntervalMs();
        verify( jobConfigRepository ).findByName( jobName );
        verify( jobConfigRepository ).save( captor.capture() );
        verifyNoMoreInteractions( jobConfigRepository, job );

        final JobConfig saved = captor.getValue();
        assertEquals( jobName, saved.getName() );
        assertEquals( jobDescription, saved.getDescription() );
        assertEquals( jobIntervalMs, saved.getIntervalMillis() );
        assertEquals( nextRunTime, saved.getNextRunTime() );
    }

    /**
     * Tests registerIfMissing falls back to default interval when job returns 0.
     */
    @Test
    void testRegisterIfMissingWithJobReturningZeroIntervalUsesFallback()
    {
        // Given
        final String name = "customJob";
        final String description = "Custom job";
        final LocalDateTime nextRunTime = LocalDateTime.now().plusMinutes( 1 );

        when( job.getJobName() ).thenReturn( name );
        when( job.getJobDescription() ).thenReturn( description );
        when( job.getIntervalMs() ).thenReturn( 0L );
        when( jobConfigRepository.findByName( name ) ).thenReturn( Optional.empty() );

        // When
        final boolean result = jobConfigService.registerIfMissing( job, DEFAULT_INTERVAL_MILLIS, nextRunTime );

        // Then
        assertTrue( result );
        final ArgumentCaptor<JobConfig> captor = ArgumentCaptor.forClass( JobConfig.class );
        verify( jobConfigRepository ).save( captor.capture() );

        final JobConfig saved = captor.getValue();
        assertEquals( DEFAULT_INTERVAL_MILLIS, saved.getIntervalMillis(), "Should use default when job interval is 0" );
    }

    /**
     * Tests registerIfMissing propagates repository lookup exceptions.
     */
    @Test
    void testRegisterIfMissingWithFindByNameRepositoryExceptionThrows()
    {
        // Given
        final String name = "brokenJob";
        final RuntimeException expected = new RuntimeException( "lookup failed" );

        when( job.getJobName() ).thenReturn( name );
        when( job.getJobDescription() ).thenReturn( "desc" );
        when( job.getIntervalMs() ).thenReturn( 0L );
        when( jobConfigRepository.findByName( name ) ).thenThrow( expected );

        // When
        final RuntimeException actual =
            assertThrows( RuntimeException.class,
                () -> jobConfigService.registerIfMissing( job, DEFAULT_INTERVAL_MILLIS, LocalDateTime.now() ) );

        // Then
        assertSame( expected, actual );
        verify( jobConfigRepository ).findByName( name );
        verify( jobConfigRepository, never() ).save( org.mockito.ArgumentMatchers.any( JobConfig.class ) );
        verifyNoMoreInteractions( jobConfigRepository, job );
    }

    /**
     * Tests registerIfMissing propagates repository save exceptions.
     */
    @Test
    void testRegisterIfMissingWithSaveRepositoryExceptionThrows()
    {
        // Given
        final String name = "saveFailureJob";
        final LocalDateTime nextRunTime = LocalDateTime.now().plusMinutes( 1 );
        final RuntimeException expected = new RuntimeException( "save failed" );

        when( job.getJobName() ).thenReturn( name );
        when( job.getJobDescription() ).thenReturn( "save failure" );
        when( job.getIntervalMs() ).thenReturn( 0L );
        when( jobConfigRepository.findByName( name ) ).thenReturn( Optional.empty() );
        when( jobConfigRepository.save( org.mockito.ArgumentMatchers.any( JobConfig.class ) ) ).thenThrow( expected );

        // When
        final RuntimeException actual =
            assertThrows( RuntimeException.class,
                () -> jobConfigService.registerIfMissing( job, DEFAULT_INTERVAL_MILLIS, nextRunTime ) );

        // Then
        assertSame( expected, actual );
        verify( jobConfigRepository ).findByName( name );
        verify( jobConfigRepository ).save( org.mockito.ArgumentMatchers.any( JobConfig.class ) );
        verifyNoMoreInteractions( jobConfigRepository );
    }
}
