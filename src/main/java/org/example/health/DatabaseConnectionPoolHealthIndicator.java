package org.example.health;

import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
@RequiredArgsConstructor
public class DatabaseConnectionPoolHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    @Override
    public Health health() {
        if (!(dataSource instanceof HikariDataSource hikari)) {
            return Health.unknown()
                    .withDetail("error", "Not a HikariDataSource")
                    .build();
        }

        var mxBean = hikari.getHikariPoolMXBean();

        return Health.up()
                .withDetail("totalConnections", mxBean.getTotalConnections())
                .withDetail("activeConnections", mxBean.getActiveConnections())
                .withDetail("idleConnections", mxBean.getIdleConnections())
                .withDetail("threadsAwaitingConnection", mxBean.getThreadsAwaitingConnection())
                .build();
    }
}