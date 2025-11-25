package com.example.tfgenerator.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.example.tfgenerator.dto.EnvironmentRequest;
import com.example.tfgenerator.dto.RdsConfig;
import com.example.tfgenerator.dto.Services;
import com.example.tfgenerator.exception.ValidationException;

@Component
public class EnvironmentValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnvironmentValidator.class);

    private static final Pattern CIDR_PATTERN =
            Pattern.compile("^([0-9]{1,3}\\.){3}[0-9]{1,3}/[0-9]{1,2}$");

    private static final Pattern NAME_PATTERN =
            Pattern.compile("^[a-z0-9][a-z0-9-]{0,62}[a-z0-9]$");

    private static final Set<String> VALID_REGIONS = Set.of(
            "us-east-1", "us-east-2",
            "us-west-1", "us-west-2",
            "eu-west-1", "eu-west-2", "eu-west-3",
            "eu-central-1", "eu-central-2",
            "ap-south-1",
            "ap-northeast-1", "ap-northeast-2", "ap-northeast-3",
            "ap-southeast-1", "ap-southeast-2",
            "ap-east-1",
            "sa-east-1",
            "ca-central-1"
    );

    private static final List<String> VALID_RDS_ENGINES = List.of("postgres", "mysql");

    public void validate(EnvironmentRequest request) {
        if (request == null) {
            throw new ValidationException("Request body is null");
        }

        List<String> errors = new ArrayList<>();

        validateName(request.getName(), errors);
        validateRegion(request.getRegion(), errors);
        validateVpcCidr(request.getVpcCidr(), errors);
        validateServices(request.getServices(), errors);

        if (!errors.isEmpty()) {
            LOGGER.warn("Validation failed: {}", errors);
            throw new ValidationException("Validation failed: " + String.join("; ", errors));
        }
    }

    private void validateName(String name, List<String> errors) {
        if (StringUtils.isBlank(name)) {
            errors.add("Field 'name' is required");
        } else if (!NAME_PATTERN.matcher(name).matches()) {
            errors.add("Field 'name' must be lowercase alphanumeric with hyphens (2–64 chars)");
        }
    }

    private void validateRegion(String region, List<String> errors) {
        if (StringUtils.isBlank(region)) {
            errors.add("Field 'region' is required");
        } else if (!VALID_REGIONS.contains(region)) {
            errors.add("Invalid region. Allowed values: " + String.join(", ", VALID_REGIONS));
        }
    }

    private void validateVpcCidr(String cidr, List<String> errors) {
        if (StringUtils.isBlank(cidr)) {
            errors.add("Field 'vpc_cidr' is required");
        } else if (!CIDR_PATTERN.matcher(cidr).matches()) {
            errors.add("Field 'vpc_cidr' must be a valid CIDR (e.g., 10.0.0.0/16)");
        }
    }

    private void validateServices(Services services, List<String> errors) {

        if (services == null) {
            errors.add("Field 'services' is required");
            return;
        }

        validateS3(services, errors);
        validateEcs(services, errors);
        validateRds(services.getRds(), errors);
    }

    private void validateS3(Services services, List<String> errors) {
        if (services.getS3Bucket() == null) {
            errors.add("Field 'services.s3_bucket' is required (true/false)");
        }
    }

    private void validateEcs(Services services, List<String> errors) {
        if (services.getEcsCluster() != null &&
            services.getEcsCluster().getEnabled() == null) {
            errors.add("Field 'services.ecs_cluster.enabled' is required (true/false)");
        }
    }

    private void validateRds(RdsConfig rds, List<String> errors) {
        if (rds == null || !Boolean.TRUE.equals(rds.getEnabled())) {
            return;
        }

        checkRequired(rds.getEngine(), "services.rds.engine", errors);
        if (rds.getEngine() != null && !VALID_RDS_ENGINES.contains(rds.getEngine())) {
            errors.add("Field 'services.rds.engine' must be postgres or mysql");
        }

        checkRequired(rds.getInstanceClass(), "services.rds.instance_class", errors);
        checkRequired(rds.getDbName(), "services.rds.db_name", errors);
        checkRequired(rds.getUsername(), "services.rds.username", errors);

        if (StringUtils.isBlank(rds.getPassword())) {
            errors.add("Field 'services.rds.password' is required");
        } else if (rds.getPassword().length() < 8) {
            errors.add("Field 'services.rds.password' must be at least 8 characters");
        }
    }

    private void checkRequired(String field, String fieldName, List<String> errors) {
        if (StringUtils.isBlank(field)) {
            errors.add("Field '" + fieldName + "' is required");
        }
    }
}

