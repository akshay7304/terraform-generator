package com.example.tfgenerator.validator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.tfgenerator.dto.EnvironmentRequest;
import com.example.tfgenerator.dto.RdsConfig;
import com.example.tfgenerator.dto.Services;
import com.example.tfgenerator.exception.ValidationException;

public class EnvironmentValidatorTest {

    private EnvironmentValidator validator;

    @BeforeEach
    public void setUp() {
        validator = new EnvironmentValidator();
    }

    @Test
    public void testValidRequest() {
        EnvironmentRequest request = createValidRequest();
        assertDoesNotThrow(() -> validator.validate(request));
    }

    private EnvironmentRequest createValidRequest() {
        EnvironmentRequest request = new EnvironmentRequest();
        request.setName("test-app");
        request.setRegion("us-east-1");
        request.setVpcCidr("10.0.0.0/16");

        Services services = new Services();
        services.setS3Bucket(true);

        RdsConfig rds = new RdsConfig();
        rds.setEnabled(true);
        rds.setEngine("postgres");
        rds.setInstanceClass("db.t3.micro");
        rds.setDbName("testdb");
        rds.setUsername("admin");
        rds.setPassword("SecurePass123!");

        services.setRds(rds);
        request.setServices(services);
        request.setTags(new HashMap<>());

        return request;
    }

    @Test
    void testValidRequest_noErrors() {
        EnvironmentRequest request = new EnvironmentRequest();
        request.setName("demo");
        request.setRegion("us-east-1");
        request.setVpcCidr("10.0.0.0/16");

        Services services = new Services();
        services.setS3Bucket(true);
        request.setServices(services);

        assertDoesNotThrow(() -> validator.validate(request));
    }

    @Test
    void testInvalidRegion_shouldThrowError() {
        EnvironmentRequest request = new EnvironmentRequest();
        request.setName("demo");
        request.setRegion("invalid-region");
        request.setVpcCidr("10.0.0.0/16");

        assertThrows(ValidationException.class,
                () -> validator.validate(request));
    }

    @Test
    void testRdsEnabled_missingFields_shouldThrowErrors() {
        EnvironmentRequest request = new EnvironmentRequest();
        request.setName("demo");
        request.setRegion("us-east-1");
        request.setVpcCidr("10.0.0.0/16");

        RdsConfig rds = new RdsConfig();
        rds.setEnabled(true);

        Services services = new Services();
        services.setRds(rds);
        request.setServices(services);

        assertThrows(ValidationException.class,
                () -> validator.validate(request));
    }

    @Test
    void testRdsEnabled_passwordTooShort_shouldThrowError() {
        EnvironmentRequest request = new EnvironmentRequest();
        request.setName("demo");
        request.setRegion("us-east-1");
        request.setVpcCidr("10.0.0.0/16");

        RdsConfig rds = new RdsConfig();
        rds.setEnabled(true);
        rds.setEngine("postgres");
        rds.setInstanceClass("db.t3.micro");
        rds.setDbName("testdb");
        rds.setUsername("admin");
        rds.setPassword("short");

        Services services = new Services();
        services.setRds(rds);
        request.setServices(services);

        assertThrows(ValidationException.class,
                () -> validator.validate(request));
    }
}
