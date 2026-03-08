package com.spring.scheduler.startup.job;

import com.spring.scheduler.jobs.Job;
import com.spring.scheduler.service.job.JobConfigService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * {@link JobStartup} unit tests.
 *
 * @author lewisjones
 */
@ExtendWith( MockitoExtension.class )
class JobStartupTest
{
    private static final Long DEFAULT_INTERVAL_MILLIS = 60000L;

    @Mock
    private JobConfigService jobConfigService;

    @Mock
    private Job jobOne;

    @Mock
    private Job jobTwo;

    /**
     * Provides registration outcomes for parameterized registration tests.
     *
     * @return creation outcome argument stream
     */
    private static Stream<Arguments> provideRegistrationOutcomes()
    {
        return Stream.of( Arguments.of( Boolean.TRUE ), Arguments.of( Boolean.FALSE ) );
    }

    /**
     * Tests onApplicationEvent with no jobs.
     */
    @Test
    void testOnApplicationEventWithNoJobsDoesNotRegisterAnyConfig()
    {
        // Given
        final JobStartup jobStartup = createStartup( List.of() );

        // When
        jobStartup.onApplicationEvent( null );

        // Then
        verifyNoInteractions( jobConfigService );
    }

    /**
     * Tests onApplicationEvent delegates job registration for a single job regardless of creation outcome.
     *
     * @param created whether the service reports a new record was created
     */
    @ParameterizedTest
    @MethodSource( "provideRegistrationOutcomes" )
    void testOnApplicationEventWithSingleJobDelegatesRegistration( final boolean created )
    {
        // Given
        final JobStartup jobStartup = createStartup( List.of( jobOne ) );

        when( jobOne.getJobName() ).thenReturn( "dailyCleanup" );
        when( jobConfigService.registerIfMissing( eq( jobOne ), eq( DEFAULT_INTERVAL_MILLIS ),
            any( LocalDateTime.class ) ) ).thenReturn( created );

        // When
        jobStartup.onApplicationEvent( null );

        // Then
        verify( jobOne ).getJobName();
        verify( jobConfigService ).registerIfMissing( eq( jobOne ), eq( DEFAULT_INTERVAL_MILLIS ),
            any( LocalDateTime.class ) );
        verifyNoMoreInteractions( jobConfigService, jobOne );
    }

    /**
     * Tests onApplicationEvent delegates registration for multiple jobs.
     */
    @Test
    void testOnApplicationEventWithMultipleJobsDelegatesRegistrationForEachJob()
    {
        // Given
        final JobStartup jobStartup = createStartup( List.of( jobOne, jobTwo ) );

        when( jobOne.getJobName() ).thenReturn( "dailyCleanup" );
        when( jobTwo.getJobName() ).thenReturn( "hourlySync" );

        when( jobConfigService.registerIfMissing( eq( jobOne ), eq( DEFAULT_INTERVAL_MILLIS ),
            any( LocalDateTime.class ) ) ).thenReturn( Boolean.TRUE );
        when( jobConfigService.registerIfMissing( eq( jobTwo ), eq( DEFAULT_INTERVAL_MILLIS ),
            any( LocalDateTime.class ) ) ).thenReturn( Boolean.FALSE );

        // When
        jobStartup.onApplicationEvent( null );

        // Then
        verify( jobOne ).getJobName();
        verify( jobTwo ).getJobName();
        verify( jobConfigService ).registerIfMissing( eq( jobOne ), eq( DEFAULT_INTERVAL_MILLIS ),
            any( LocalDateTime.class ) );
        verify( jobConfigService ).registerIfMissing( eq( jobTwo ), eq( DEFAULT_INTERVAL_MILLIS ),
            any( LocalDateTime.class ) );
        verifyNoMoreInteractions( jobConfigService, jobOne, jobTwo );
    }

    /**
     * Tests onApplicationEvent propagates service exceptions.
     */
    @Test
    void testOnApplicationEventWithServiceExceptionThrows()
    {
        // Given
        final RuntimeException expected = new RuntimeException( "database unavailable" );
        final JobStartup jobStartup = createStartup( List.of( jobOne ) );

        when( jobOne.getJobName() ).thenReturn( "dailyCleanup" );
        when( jobConfigService.registerIfMissing( eq( jobOne ), eq( DEFAULT_INTERVAL_MILLIS ),
            any( LocalDateTime.class ) ) ).thenThrow( expected );

        // When
        final RuntimeException actual =
            assertThrows( RuntimeException.class, () -> jobStartup.onApplicationEvent( null ) );

        // Then
        org.junit.jupiter.api.Assertions.assertSame( expected, actual );
        verify( jobOne ).getJobName();
        verify( jobConfigService, times( 1 ) ).registerIfMissing( eq( jobOne ), eq( DEFAULT_INTERVAL_MILLIS ),
            any( LocalDateTime.class ) );
        verifyNoMoreInteractions( jobConfigService, jobOne );
    }

    /**
     * Creates a startup instance with test-configured interval.
     *
     * @param jobs the jobs available at startup
     * @return configured {@link JobStartup}
     */
    private JobStartup createStartup( final List<Job> jobs )
    {
        final JobStartup jobStartup = new JobStartup( jobs, jobConfigService );
        ReflectionTestUtils.setField( jobStartup, "defaultIntervalMillis", DEFAULT_INTERVAL_MILLIS );
        return jobStartup;
    }
}

