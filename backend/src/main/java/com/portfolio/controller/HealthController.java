package com.portfolio.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HealthController {

    private final HealthEndpoint healthEndpoint;

    // The external monitor polls this path, but the edge does not route /actuator/health, so the
    // aggregated Actuator status (database included) is mirrored here without the component details.
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        boolean up = Status.UP.equals(healthEndpoint.health().getStatus());
        return ResponseEntity
                .status(up ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "status", up ? "UP" : "DOWN",
                        "service", "Portfolio API",
                        "timestamp", System.currentTimeMillis()
                ));
    }
}
