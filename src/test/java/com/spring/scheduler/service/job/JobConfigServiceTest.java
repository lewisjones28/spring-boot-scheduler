package com.spring.scheduler.service.job;

import com.spring.scheduler.domain.job.JobConfig;
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

    @InjectMocks
    private JobConfigService jobConfigService;

    /**
     * Provides valid registerIfMissing input combinations.
     *
     * @return input argument stream for parameterized test coverage
     */
    private static Stream<Arguments> provideRegisterInputs()
    {
        return Stream.of( Arguments.of( "dailyCleanup", "Daily cleanup", DEFAULT_INTERVAL_MILLIS,
                LocalDateTime.now().plusMinutes( 1 ) ),
            Arguments.of( "hourlySync", "Hourly sync", HOURLY_INTERVAL_MILLIS, LocalDateTime.now().plusMinutes( 2 ) ),
            Arguments.of( "nightlyRollup", null, NIGHTLY_INTERVAL_MILLIS, LocalDateTime.now().plusMinutes( 3 ) ) );
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
        when( jobConfigRepository.findByName( name ) ).thenReturn( Optional.of( existing ) );

        // When
        final boolean result =
            jobConfigService.registerIfMissing( name, description, DEFAULT_INTERVAL_MILLIS, nextRunTime );

        // Then
        assertFalse( result );
        verify( jobConfigRepository ).findByName( name );
        verify( jobConfigRepository, never() ).save( org.mockito.ArgumentMatchers.any( JobConfig.class ) );
        verifyNoMoreInteractions( jobConfigRepository );
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
        final Long intervalMillis = HOURLY_INTERVAL_MILLIS;
        final LocalDateTime nextRunTime = LocalDateTime.now().plusMinutes( 5 );
        when( jobConfigRepository.findByName( name ) ).thenReturn( Optional.empty() );

        // When
        final boolean result = jobConfigService.registerIfMissing( name, description, intervalMillis, nextRunTime );

        // Then
        assertTrue( result );
        final ArgumentCaptor<JobConfig> captor = ArgumentCaptor.forClass( JobConfig.class );
        verify( jobConfigRepository ).findByName( name );
        verify( jobConfigRepository ).save( captor.capture() );
        verifyNoMoreInteractions( jobConfigRepository );

        final JobConfig saved = captor.getValue();
        assertEquals( name, saved.getName() );
        assertEquals( description, saved.getDescription() );
        assertEquals( intervalMillis, saved.getIntervalMillis() );
        assertEquals( nextRunTime, saved.getNextRunTime() );
    }

    /**
     * Tests registerIfMissing maps multiple valid input combinations into saved entities.
     *
     * @param name           the job name
     * @param description    the job description
     * @param intervalMillis the configured interval
     * @param nextRunTime    the next run timestamp
     */
    @ParameterizedTest
    @MethodSource( "provideRegisterInputs" )
    void testRegisterIfMissingWithValidInputsMapsFieldsCorrectly( final String name, final String description,
        final Long intervalMillis, final LocalDateTime nextRunTime )
    {
        // Given
        when( jobConfigRepository.findByName( name ) ).thenReturn( Optional.empty() );

        // When
        final boolean result = jobConfigService.registerIfMissing( name, description, intervalMillis, nextRunTime );

        // Then
        assertTrue( result );
        final ArgumentCaptor<JobConfig> captor = ArgumentCaptor.forClass( JobConfig.class );
        verify( jobConfigRepository ).findByName( name );
        verify( jobConfigRepository ).save( captor.capture() );
        verifyNoMoreInteractions( jobConfigRepository );

        final JobConfig saved = captor.getValue();
        assertEquals( name, saved.getName() );
        assertEquals( description, saved.getDescription() );
        assertEquals( intervalMillis, saved.getIntervalMillis() );
        assertEquals( nextRunTime, saved.getNextRunTime() );
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
        when( jobConfigRepository.findByName( name ) ).thenThrow( expected );

        // When
        final RuntimeException actual = assertThrows( RuntimeException.class,
            () -> jobConfigService.registerIfMissing( name, "desc", DEFAULT_INTERVAL_MILLIS, LocalDateTime.now() ) );

        // Then
        assertSame( expected, actual );
        verify( jobConfigRepository ).findByName( name );
        verify( jobConfigRepository, never() ).save( org.mockito.ArgumentMatchers.any( JobConfig.class ) );
        verifyNoMoreInteractions( jobConfigRepository );
    }

    /**
     * Tests registerIfMissing propagates repository save exceptions.
     */
    @Test
    void testRegisterIfMissingWithSaveRepositoryExceptionThrows()
    {
        // Given
        final String name = "saveFailureJob";
        final String description = "save failure";
        final LocalDateTime nextRunTime = LocalDateTime.now().plusMinutes( 1 );
        final RuntimeException expected = new RuntimeException( "save failed" );
        when( jobConfigRepository.findByName( name ) ).thenReturn( Optional.empty() );
        when( jobConfigRepository.save( org.mockito.ArgumentMatchers.any( JobConfig.class ) ) ).thenThrow( expected );

        // When
        final RuntimeException actual = assertThrows( RuntimeException.class,
            () -> jobConfigService.registerIfMissing( name, description, DEFAULT_INTERVAL_MILLIS, nextRunTime ) );

        // Then
        assertSame( expected, actual );
        verify( jobConfigRepository ).findByName( name );
        verify( jobConfigRepository ).save( org.mockito.ArgumentMatchers.any( JobConfig.class ) );
        verifyNoMoreInteractions( jobConfigRepository );
    }
}
