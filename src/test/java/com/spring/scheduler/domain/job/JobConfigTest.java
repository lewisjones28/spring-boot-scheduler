package com.spring.scheduler.domain.job;

import com.spring.scheduler.common.job.JobStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link JobConfig} unit tests.
 *
 * @author lewisjones
 */
class JobConfigTest
{

    private static final Long DEFAULT_INTERVAL_MILLIS = 3600000L;
    private static final Integer DIFFERENT_ID = 999;
    private static final Long DIFFERENT_INTERVAL_MILLIS = 999999L;

    /**
     * Test equals with same name returns true.
     */
    @Test
    void testEqualsWithSameName()
    {
        // Given
        final String jobName = "daily-cleanup";
        final JobConfig config1 = createJobConfig( jobName );
        final JobConfig config2 = createJobConfig( jobName );

        // When
        final boolean result = config1.equals( config2 );

        // Then
        assertTrue( result, "JobConfigs with same name should be equal" );
    }

    /**
     * Test equals with different names returns false.
     */
    @Test
    void testEqualsWithDifferentNames()
    {
        // Given
        final JobConfig config1 = createJobConfig( "daily-cleanup" );
        final JobConfig config2 = createJobConfig( "hourly-sync" );

        // When
        final boolean result = config1.equals( config2 );

        // Then
        assertFalse( result, "JobConfigs with different names should not be equal" );
    }

    /**
     * Test equals with same instance returns true.
     */
    @Test
    void testEqualsWithSameInstance()
    {
        // Given
        final JobConfig config = createJobConfig( "daily-cleanup" );

        // When
        final boolean result = config.equals( config );

        // Then
        assertTrue( result, "JobConfig should equal itself" );
    }

    /**
     * Test equals with null returns false.
     */
    @Test
    void testEqualsWithNull()
    {
        // Given
        final JobConfig config = createJobConfig( "daily-cleanup" );

        // When
        final boolean result = config.equals( null );

        // Then
        assertFalse( result, "JobConfig should not equal null" );
    }

    /**
     * Test equals with different class returns false.
     */
    @Test
    void testEqualsWithDifferentClass()
    {
        // Given
        final JobConfig config = createJobConfig( "daily-cleanup" );
        final String differentClassObject = "Not a JobConfig";

        // When
        final boolean result = config.equals( differentClassObject );

        // Then
        assertFalse( result, "JobConfig should not equal object of different class" );
    }

    /**
     * Test hashCode consistency for same name.
     */
    @Test
    void testHashCodeWithSameName()
    {
        // Given
        final String jobName = "daily-cleanup";
        final JobConfig config1 = createJobConfig( jobName );
        final JobConfig config2 = createJobConfig( jobName );

        // When
        final int hash1 = config1.hashCode();
        final int hash2 = config2.hashCode();

        // Then
        assertEquals( hash1, hash2, "JobConfigs with same name should have same hashCode" );
    }

    /**
     * Test hashCode difference for different names.
     */
    @Test
    void testHashCodeWithDifferentNames()
    {
        // Given
        final JobConfig config1 = createJobConfig( "daily-cleanup" );
        final JobConfig config2 = createJobConfig( "hourly-sync" );

        // When
        final int hash1 = config1.hashCode();
        final int hash2 = config2.hashCode();

        // Then
        assertNotEquals( hash1, hash2, "JobConfigs with different names should have different hashCodes" );
    }

    /**
     * Test hashCode consistency across multiple calls.
     */
    @Test
    void testHashCodeConsistency()
    {
        // Given
        final JobConfig config = createJobConfig( "daily-cleanup" );

        // When
        final int hash1 = config.hashCode();
        final int hash2 = config.hashCode();
        final int hash3 = config.hashCode();

        // Then
        assertEquals( hash1, hash2, "hashCode should be consistent" );
        assertEquals( hash2, hash3, "hashCode should be consistent across multiple calls" );
    }

    /**
     * Test equals ignores non-business key fields.
     *
     * @param config1 the first JobConfig instance
     * @param config2 the second JobConfig instance
     */
    @ParameterizedTest
    @MethodSource( "provideJobConfigsWithDifferentNonKeyFields" )
    void testEqualsIgnoresNonBusinessKeyFields( final JobConfig config1, final JobConfig config2 )
    {
        // When
        final boolean result = config1.equals( config2 );

        // Then
        assertTrue( result, "JobConfigs with same name but different non-key fields should be equal" );
    }

    /**
     * Test constructor with all parameters.
     */
    @Test
    void testConstructorWithAllParameters()
    {
        // Given
        final String name = "daily-cleanup";
        final String description = "Daily cleanup task";
        final Long intervalMillis = 86400000L;
        final LocalDateTime nextRunTime = LocalDateTime.now().plusDays( 1 );

        // When
        final JobConfig config = new JobConfig( name, description, intervalMillis, nextRunTime );

        // Then
        assertNotNull( config, "JobConfig should be created" );
        assertEquals( name, config.getName(), "Name should match" );
        assertEquals( description, config.getDescription(), "Description should match" );
        assertEquals( intervalMillis, config.getIntervalMillis(), "Interval should match" );
        assertEquals( nextRunTime, config.getNextRunTime(), "Next run time should match" );
        assertEquals( JobStatus.IDLE, config.getStatus(), "Default status should be IDLE" );
        assertFalse( config.isEnabled(), "Default enabled should be false" );
    }

    /**
     * Test getters and setters.
     */
    @Test
    void testGettersAndSetters()
    {
        // Given
        final JobConfig config = createJobConfig( "test-job" );
        final Integer id = 123;
        final String newName = "updated-job";
        final String newDescription = "Updated description";
        final JobStatus newStatus = JobStatus.RUNNING;
        final LocalDateTime newNextRunTime = LocalDateTime.now().plusHours( 2 );
        final LocalDateTime newLastRunTime = LocalDateTime.now().minusHours( 1 );
        final Long newInterval = 3600000L;
        final boolean newEnabled = true;

        // When
        config.setId( id );
        config.setName( newName );
        config.setDescription( newDescription );
        config.setStatus( newStatus );
        config.setNextRunTime( newNextRunTime );
        config.setLastRunTime( newLastRunTime );
        config.setIntervalMillis( newInterval );
        config.setEnabled( newEnabled );

        // Then
        assertEquals( id, config.getId(), "ID should be set" );
        assertEquals( newName, config.getName(), "Name should be updated" );
        assertEquals( newDescription, config.getDescription(), "Description should be updated" );
        assertEquals( newStatus, config.getStatus(), "Status should be updated" );
        assertEquals( newNextRunTime, config.getNextRunTime(), "Next run time should be updated" );
        assertEquals( newLastRunTime, config.getLastRunTime(), "Last run time should be updated" );
        assertEquals( newInterval, config.getIntervalMillis(), "Interval should be updated" );
        assertTrue( config.isEnabled(), "Enabled should be updated" );
    }

    /**
     * Provides JobConfig instances with same name but different non-key fields for parameterized testing.
     *
     * @return stream of argument pairs containing JobConfigs with same name but different properties
     */
    private static Stream<Arguments> provideJobConfigsWithDifferentNonKeyFields()
    {
        final String sameName = "daily-cleanup";
        final LocalDateTime now = LocalDateTime.now();

        final JobConfig config1 = createJobConfig( sameName );
        config1.setId( 1 );
        config1.setStatus( JobStatus.IDLE );
        config1.setEnabled( false );

        final JobConfig config2 = createJobConfig( sameName );
        config2.setId( DIFFERENT_ID );
        config2.setStatus( JobStatus.RUNNING );
        config2.setEnabled( true );
        config2.setLastRunTime( now );

        final JobConfig config3 = createJobConfig( sameName );
        config3.setDescription( "Different description" );
        config3.setIntervalMillis( DIFFERENT_INTERVAL_MILLIS );

        return Stream.of(
            Arguments.of( config1, config2 ),
            Arguments.of( config1, config3 ),
            Arguments.of( config2, config3 )
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

}
