package ru.lsv.librarian2.health;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;

import jakarta.enterprise.context.ApplicationScoped;
import ru.lsv.librarian2.models.Library;

@Readiness
@ApplicationScoped
public class ReadinessCheck implements HealthCheck {

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.named("Database connection health check");        
        try {
            Library.count();
            responseBuilder.up();
        } catch (Throwable e) {
            responseBuilder.down();
        }
        return responseBuilder.build();
    }
    
}
