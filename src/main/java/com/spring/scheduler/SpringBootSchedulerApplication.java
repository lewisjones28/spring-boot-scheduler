package com.spring.scheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Boot Scheduler Application. This application demonstrates how to use Spring's scheduling
 * capabilities to run tasks at specified intervals.
 *
 * @author lewisjones
 */
@SpringBootApplication
@EnableScheduling
public class SpringBootSchedulerApplication
{

    /**
     * The main method serves as the entry point for the Spring Boot application. It uses SpringApplication.run()
     * to launch the application, which will start the embedded server and initialize the Spring context.
     *
     * @param args command-line arguments passed to the application
     */
    static void main( final String[] args )
    {
        SpringApplication.run( SpringBootSchedulerApplication.class, args );
    }

}
