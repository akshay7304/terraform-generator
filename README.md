# Terraform Environment Generator

This project provides a REST API for dynamically generating Terraform configuration files based on an environment specification.  
It is built using Spring Boot and FreeMarker templates and is designed to produce production-grade Terraform code for AWS resources such as VPC, S3, RDS, and ECS.

The application validates the request payload, processes the templates, and returns the generated Terraform files as JSON.

---

## Features

- Generates Terraform files programmatically using FreeMarker templates.
- Strict validation of request data with aggregated error messages.
- Supports generation of:
  - VPC
  - Subnets (public and private)
  - Internet Gateway and NAT Gateway
  - S3 bucket (optional)
  - RDS (PostgreSQL/MySQL, optional)
  - ECS Cluster (optional)
- Clean architecture with DTOs, services, validators, and custom exceptions.
- Unit tests included for validation and generation logic.

---

## Technology Stack

- Java 17+
- Spring Boot 2.x
- Maven
- FreeMarker Template Engine
- JUnit 5

---

## Project Structure

src/main/java/com/example/tfgenerator/
  ├── controller/
  │      EnvironmentController.java
  ├── service/
  │      TerraformGenerationService.java
  │      TerraformTemplateService.java
  ├── validator/
  │      EnvironmentValidator.java
  ├── dto/
  │      EnvironmentRequest.java
  │      Services.java
  │      RdsConfig.java
  │      TerraformResponse.java
  ├── exception/
  │      ValidationException.java
  └── TerraformGeneratorApplication.java

src/main/resources/templates/terraform/
  ├── main.tf.ftl
  ├── variables.tf.ftl
  ├── vpc.tf.ftl
  ├── services_s3.tf.ftl
  ├── services_rds.tf.ftl
  ├── services_ecs.tf.ftl
  ├── outputs.tf.ftl
  └── terraform.tfvars.ftl

---

## How to Run

### 1. Build the project

```
mvn clean install
```

### 2. Start the application

```
mvn spring-boot:run
```

Application runs at:

```
http://localhost:8080
```

---

## API Endpoint

### POST  
```
http://localhost:8080/api/v1/environments
```

This endpoint accepts a JSON request describing the environment and returns the generated Terraform files.

---

## Example Request

```
{
  "name": "demo-app",
  "region": "us-east-1",
  "vpc_cidr": "10.0.0.0/16",
  "services": {
    "s3_bucket": true,
    "rds": {
      "enabled": true,
      "engine": "postgres",
      "instance_class": "db.t3.micro",
      "db_name": "mydb",
      "username": "admin",
      "password": "StrongPass123!"
    },
    "ecs_cluster": {
      "enabled": true
    }
  },
  "tags": {
    "owner": "platform-team",
    "env": "dev"
  }
}
```

---

## Example Successful Response (Trimmed)

```
{
  "success": true,
  "data": {
    "main_tf": "...",
    "variables_tf": "...",
    "vpc_tf": "...",
    "services_s3_tf": "...",
    "services_rds_tf": "...",
    "services_ecs_tf": "...",
    "outputs_tf": "...",
    "terraform_tfvars": "..."
  }
}
```

Each field contains the full Terraform file content.

---

## Validation Rules

Validation is performed inside `EnvironmentValidator`.

### Required Fields:
- name
- region (must be one of the supported AWS regions)
- vpc_cidr
- services

### RDS Validation (when enabled)
- engine must be "postgres" or "mysql"
- instance_class is required
- db_name is required
- username is required
- password must be at least 8 characters

If validation fails:
- All errors are aggregated
- A ValidationException is thrown
- HTTP 400 is returned

---

## Unit Tests

Unit tests included for:

- Valid request scenarios
- Invalid request scenarios
- RDS validation failures
- Terraform file generation logic (sample permutations)

Run tests:

```
mvn test
```

---

## Assumptions

- AWS is the only cloud supported in this version.
- A standard production VPC layout is always generated.
- RDS instances are always placed in the private subnet.
- S3 bucket names append AWS account ID for uniqueness.
- ECS uses Fargate and Fargate Spot capacity providers.
- Returned Terraform is not applied by this service; applying is left to the user.

---

## Extending to Multi-Cloud

To support Azure, GCP, or hybrid deployments:

1. Introduce a cloud field in the request.
2. Maintain separate template directories:
   templates/terraform/aws/
   templates/terraform/azure/
   templates/terraform/gcp/
3. Implement a strategy pattern for generation:
   TerraformGeneratorStrategy
   AwsTerraformGenerator
   AzureTerraformGenerator
   GcpTerraformGenerator
4. Add cloud-specific resources.
5. Introduce reusable Terraform modules.

---

## Logging

Logs are written to:

```
/logs/app.log
```

Logs include incoming requests, validation errors, template rendering status, and generation steps.

