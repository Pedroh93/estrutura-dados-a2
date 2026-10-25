package com.a2.backend.controller;

import java.time.Instant;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping
    public Map<String, Object> health() {
        String database = jdbcTemplate.queryForObject("SELECT version()", String.class);
        return Map.of(
                "status", "UP",
                "database", database,
                "timestamp", Instant.now().toString());
    }
}
