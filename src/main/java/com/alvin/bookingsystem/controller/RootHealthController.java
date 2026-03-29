package com.alvin.bookingsystem.controller;

import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes the same payload as {@code GET /actuator/health} at {@code GET /} for load balancers and simple probes.
 */
@RestController
public class RootHealthController {

    private final HealthEndpoint healthEndpoint;

    public RootHealthController(HealthEndpoint healthEndpoint) {
        this.healthEndpoint = healthEndpoint;
    }

    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public HealthComponent rootHealth() {
        return healthEndpoint.health();
    }
}
