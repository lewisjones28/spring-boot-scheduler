package com.spring.scheduler.exception.job;

/**
 * Exception thrown when scheduled job execution state transitions fail.
 *
 * @author lewisjones
 */
public class JobExecutionException extends RuntimeException
{

    /**
     * Creates an exception with message.
     *
     * @param message exception message
     */
    public JobExecutionException( final String message )
    {
        super( message );
    }

    /**
     * Creates an exception with message and cause.
     *
     * @param message exception message
     * @param cause root cause
     */
    public JobExecutionException( final String message, final Throwable cause )
    {
        super( message, cause );
    }
}

