package com.spring.scheduler;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link SpringBootSchedulerApplicationTests} is a test class for the {@link SpringBootSchedulerApplication}.
 *
 * @author lewisjones
 */
@SpringBootTest
class SpringBootSchedulerApplicationTests
{

    /**
     * Test to verify that the Spring application context loads successfully. This test will pass if the context
     * loads without any exceptions, indicating that the application is properly configured.
     */
    @Test
    void contextLoads()
    {
        assertTrue( Boolean.TRUE, "The application context should load successfully." );
    }

}
