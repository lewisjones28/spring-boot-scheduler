# Spring Boot Scheduler

Internal scheduling framework built with Spring Boot, JPA, and Flyway.  
It discovers `Job` implementations at startup, stores their configuration in the database, and executes due jobs on a polling interval with safe claim logic for multi-instance deployments.

## Tech Stack

- Java 25
- Spring Boot 4.x.x
- Spring Scheduling
- Spring Data JPA
- Flyway
- PostgreSQL (runtime)
- H2 (tests)
- Maven
- Checkstyle + JaCoCo

## How It Works

1. On application startup, `JobStartup` scans all Spring-managed `Job` beans.
2. Each job is registered in `job_configs` if missing.
3. `JobTask` runs on a fixed delay (`scheduler.polling-interval-ms`) and fetches due jobs.
4. Each due job is claimed atomically via DB update to avoid duplicate execution across nodes/pods.
5. A `job_executions` record is created with status `RUNNING`.
6. On success:

- job status returns to `IDLE`
- next run time is scheduled using `interval_millis`
- execution status is set to `SUCCESS`

7. On failure:

- job status returns to `IDLE`
- retry is scheduled after 5 minutes
- execution status is set to `FAILED` with error message and stack trace

## Project Structure

- `src/main/java/com/spring/scheduler/jobs/`: Job interface and implementations
- `src/main/java/com/spring/scheduler/startup/job/`: Startup registration logic
- `src/main/java/com/spring/scheduler/task/job/`: Polling scheduler task
- `src/main/java/com/spring/scheduler/service/job/`: Job orchestration and persistence services
- `src/main/java/com/spring/scheduler/repository/job/`: JPA repositories
- `src/main/java/com/spring/scheduler/domain/job/`: Entities (`JobConfig`, `JobExecution`)
- `src/main/resources/db/migration/`: Flyway migrations

## Database Schema

Flyway migrations create and index:

- `job_configs`
- stores job metadata, state, interval, and scheduling timestamps
- `job_executions`
- stores execution history, status, error details, and executor identity

## Configuration

### Default (`src/main/resources/application.yaml`)

- `SPRING_DATASOURCE_URL` default: `jdbc:postgresql://localhost:5432/scheduler`
- `SPRING_DATASOURCE_USERNAME` default: `postgres`
- `SPRING_DATASOURCE_PASSWORD` default: `password`
- `scheduler.default-job-interval-ms` default: `30000`
- `scheduler.polling-interval-ms` default: `30000`

### Local Profile (`src/main/resources/application-local.yaml`)

- polling interval is shorter for local feedback (`5000` ms)
- default interval is shorter (`10000` ms)
- SQL logging enabled

Run with local profile:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

If you do not use Maven Wrapper in your environment, use `mvn` instead:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

## Getting Started

### Prerequisites

- Java 25
- Maven
- PostgreSQL running locally (or update datasource properties)

### Run

```bash
./mvnw spring-boot:run
```

Or:

```bash
mvn spring-boot:run
```

### Build

```bash
./mvnw clean verify
```

### Test

```bash
./mvnw test
```

Tests use H2 in-memory DB (`src/test/resources/application.yaml`).

## Sample Jobs Included

- `success-job`: logs successful execution
- `failure-job`: throws exception to demonstrate failure handling
- `long-running-job`: simulates a long-running process

## Adding a New Job

1. Create a Spring bean implementing `com.spring.scheduler.jobs.Job`.
2. Return a unique value from `getJobName()`.
3. Implement `execute()` with your business logic.
4. Optionally override:

- `getIntervalMs()` for custom interval
- `onSuccess()` and `onFailure(Throwable)` hooks

5. Start the application; `JobStartup` auto-registers it in `job_configs`.

Minimal example:

```java
@Component
public class ReportJob implements Job {

    @Override
    public void execute() {
        // business logic
    }

    @Override
    public String getJobName() {
        return "report-job";
    }

    @Override
    public String getJobDescription() {
        return "Generates scheduled reports";
    }

    @Override
    public long getIntervalMs() {
        return 15 * 60 * 1000L;
    }
}
```

## Multi-Instance Safety

`JobConfigRepository.claimJob(...)` performs an atomic status transition (`IDLE` -> `RUNNING`) with timing guards in SQL. This prevents two nodes from running the same job at the same time.

## Quality Gates

- Checkstyle runs in `validate` phase for main and test sources
- JaCoCo runs during tests and generates aggregate report

JaCoCo report location after tests:

- `target/site/jacoco-aggregate/index.html`

## Notes

- Executor identity is stored using `POD_NAME` or `HOSTNAME` when available.
- Failed jobs are retried after 5 minutes by default.
