package com.somamanoj.incidentmanagement;

import com.somamanoj.incidentmanagement.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Starting point of the Spring Boot application.
 * @SpringBootApplication turns on component scanning, auto-configuration, and configuration support.
 */
@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class IncidentManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(IncidentManagementApplication.class, args);
    }
}
