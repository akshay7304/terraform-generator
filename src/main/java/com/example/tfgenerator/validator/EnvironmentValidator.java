package com.example.tfgenerator.validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
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

    private static final Set<String> VALID_REGIONS = new HashSet<>(Arrays.asList(
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
    	));


    private static final List<String> VALID_RDS_ENGINES = Arrays.asList(
            "postgres", "mysql"
    );

    public void validate(EnvironmentRequest request) {
        LOGGER.debug("Validating request: {}", request);
        List<String> errors = new ArrayList<>();

        if (request == null) {
            throw new ValidationException("Request body is null");
        }

        if (StringUtils.isBlank(request.getName())) {
            errors.add("Field 'name' is required");
        } else if (!NAME_PATTERN.matcher(request.getName()).matches()) {
            errors.add("Field 'name' must be lowercase alphanumeric with hyphens, 2-64 characters, starting and ending with alphanumeric");
        }

        if (StringUtils.isBlank(request.getRegion())) {
            errors.add("Field 'region' is required");
        } else if (!VALID_REGIONS.contains(request.getRegion())) {
            errors.add("Field 'region' must be one of: " + String.join(", ", VALID_REGIONS));
        }

        if (StringUtils.isBlank(request.getVpcCidr())) {
            errors.add("Field 'vpc_cidr' is required");
        } else if (!CIDR_PATTERN.matcher(request.getVpcCidr()).matches()) {
            errors.add("Field 'vpc_cidr' must be a valid CIDR notation (e.g., 10.0.0.0/16)");
        }

        if (request.getServices() == null) {
            errors.add("Field 'services' is required");
        } else {
            validateServices(request, errors);
        }

        if (!errors.isEmpty()) {
            LOGGER.warn("Validation failed: {}", errors);
            throw new ValidationException("Validation failed: " + String.join("; ", errors));
        }

        LOGGER.debug("Validation passed for request: {}", request.getName());
    }

    private void validateServices(EnvironmentRequest request, List<String> errors) {

        Services services = request.getServices();

        if (services == null) {
            errors.add("Field 'services' is required");
            return;
        }

        if (services.getS3Bucket() == null) {
            errors.add("Field 'services.s3_bucket' is required (true/false)");
        }

        if (services.getEcsCluster() != null) {
            if (services.getEcsCluster().getEnabled() == null) {
                errors.add("Field 'services.ecs_cluster.enabled' is required (true/false)");
            }
        }

        RdsConfig rds = services.getRds();

        if (rds != null && Boolean.TRUE.equals(rds.getEnabled())) {

            if (StringUtils.isBlank(rds.getEngine())) {
                errors.add("Field 'services.rds.engine' is required when RDS is enabled");
            } else if (!VALID_RDS_ENGINES.contains(rds.getEngine())) {
                errors.add("Field 'services.rds.engine' must be one of: "
                        + String.join(", ", VALID_RDS_ENGINES));
            }

            if (StringUtils.isBlank(rds.getInstanceClass())) {
                errors.add("Field 'services.rds.instance_class' is required when RDS is enabled");
            }

            if (StringUtils.isBlank(rds.getDbName())) {
                errors.add("Field 'services.rds.db_name' is required when RDS is enabled");
            }

            if (StringUtils.isBlank(rds.getUsername())) {
                errors.add("Field 'services.rds.username' is required when RDS is enabled");
            }

            if (StringUtils.isBlank(rds.getPassword())) {
                errors.add("Field 'services.rds.password' is required when RDS is enabled");
            } else if (rds.getPassword().length() < 8) {
                errors.add("Field 'services.rds.password' must be at least 8 characters");
            }
        }
    }

}
