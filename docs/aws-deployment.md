# AWS Deployment Notes

This project is prepared to run the API Server with environment-driven configuration.

## API Server Profiles

- `local`: default profile for local PostgreSQL from Docker Compose.
- `smoke`: test profile using in-memory H2 for fast context and migration checks.
- `prod`: AWS-oriented profile that reads database settings from environment variables.

## Production Environment Variables

```text
SPRING_PROFILES_ACTIVE=prod
DATABASE_URL=jdbc:postgresql://<rds-endpoint>:5432/<database>
DATABASE_USERNAME=<username>
DATABASE_PASSWORD=<password>
DATABASE_MAX_POOL_SIZE=10
DATABASE_MIN_IDLE=2
DATABASE_CONNECTION_TIMEOUT_MS=30000
SERVER_PORT=8080
SHUTDOWN_TIMEOUT=30s
```

For AWS, store sensitive values such as `DATABASE_USERNAME` and `DATABASE_PASSWORD` in a managed secret store or task runtime secret injection. Do not commit real credentials.

## Health Checks

Spring Actuator health probes are enabled:

```text
/actuator/health
/actuator/health/liveness
/actuator/health/readiness
```

These endpoints are intended for container health checks and load balancer target health.

