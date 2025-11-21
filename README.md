# Terraform Environment Generator

This project provides a REST API for dynamically generating Terraform configuration files based on an environment specification.
It is built using Spring Boot and FreeMarker templates and is designed to produce production-grade Terraform code for AWS resources such as VPC, S3, RDS, and ECS.

The application validates the request payload, processes the templates, and returns the generated Terraform files either as JSON or as a downloadable ZIP archive.

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
- Clean architecture with DTOs, services, validators, templates, and custom exceptions.
- Includes unit tests for validation and generation logic.
- Supports ZIP download endpoint for full project creation.

---

## Technology Stack

- Java 17+
- Spring Boot
- Maven
- FreeMarker Template Engine
- JUnit 5

---

## Project Structure

```
src/main/java/com/example/tfgenerator/
├── controller/
│   └── EnvironmentController.java
├── service/
│   ├── TerraformGenerationService.java
│   └── TerraformTemplateService.java
├── validator/
│   └── EnvironmentValidator.java
├── dto/
│   ├── EnvironmentRequest.java
│   ├── Services.java
│   ├── RdsConfig.java
│   └── TerraformResponse.java
├── exception/
│   └── ValidationException.java
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
```

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

The application runs at:

```
http://localhost:8080
```

It's deployed on Render:

```
https://terraform-generator-demo.onrender.com
```

(If the cloud deployment is inactive, use localhost.)

---

## API Endpoints

### 1. Generate Terraform (JSON response)

**POST**
```
http://localhost:8080/api/v1/environments
https://terraform-generator-demo.onrender.com/api/v1/environments
```

**CURL TO Test**
```
curl --location 'https://terraform-generator-demo.onrender.com/api/v1/environments' \
--header 'Content-Type: application/json' \
--data '{
  "name": "demo-app",
  "region": "us-east-1",
  "vpc_cidr": "10.0.0.0/16",

  "services": {
    "s3_bucket": true,

    "rds": {
      "enabled": true,
      "engine": "postgres",
      "instance_class": "db.t3.micro",
      "db_name": "myappdb",
      "username": "adminuser",
      "password": "AdminPass123!"
    },

    "ecs_cluster": {
      "enabled": true
    }
  },

  "tags": {
    "owner": "platform-team",
    "env": "dev",
    "project": "full-stack-deployment"
  }
}
'

```

### 2. Download ZIP of Terraform project

**POST**
```
http://localhost:8080/api/v1/download
https://terraform-generator-demo.onrender.com/api/v1/download
```

**CURL TO Test**

```
curl --location 'https://terraform-generator-demo.onrender.com/api/v1/download' \
--header 'Content-Type: application/json' \
--data '{
  "name": "demo-app",
  "region": "us-east-1",
  "vpc_cidr": "10.0.0.0/16",

  "services": {
    "s3_bucket": true,

    "rds": {
      "enabled": true,
      "engine": "postgres",
      "instance_class": "db.t3.micro",
      "db_name": "myappdb",
      "username": "adminuser",
      "password": "AdminPass123!"
    },

    "ecs_cluster": {
      "enabled": true
    }
  },

  "tags": {
    "owner": "platform-team",
    "env": "dev",
    "project": "full-stack-deployment"
  }
}
'

```

The browser/Postman will download `{name}.zip`. Please on postman while downloading zip click the button Send and Download.

---

## Example Request (Ideal)

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
      "db_name": "myappdb",
      "username": "adminuser",
      "password": "AdminPass123!"
    },

    "ecs_cluster": {
      "enabled": true
    }
  },

  "tags": {
    "owner": "platform-team",
    "env": "dev",
    "project": "full-stack-deployment"
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

---

## Validation Rules

Validation occurs in `EnvironmentValidator`.

### Required Fields:
- name
- region (must be valid AWS region)
- vpcCidr
- services

### RDS Rules (when enabled):
- engine must be postgres/mysql
- instanceClass required
- dbName required
- username required
- password ≥ 8 chars

Errors are aggregated and returned as:

```
HTTP 400 Bad Request
```

---

## ZIP Download

The `/download` endpoint automatically returns a ZIP containing:

```
main.tf
variables.tf
vpc.tf
services_s3.tf
services_rds.tf
services_ecs.tf
outputs.tf
terraform.tfvars
```

Filename = `{name}.zip`

---

## Unit Tests Included

Tests cover:
- Request validation
- RDS validation
- Missing field validation
- Terraform generation permutations

Run tests:

```
mvn test
```

---

## Assumptions

- AWS is the only supported cloud.
- RDS is always created in private subnet.
- S3 bucket naming includes AWS account ID.
- ECS uses Fargate + Fargate Spot.
- Terraform is only generated, not executed.

---

## Extending for Multi-Cloud

To support Azure/GCP:

1. Add `cloud` field in request.
2. Create folders:
   ```
   templates/terraform/aws/
   templates/terraform/azure/
   templates/terraform/gcp/
   ```
3. Implement strategy pattern:
   - `TerraformGeneratorStrategy`
   - `AwsTerraformGenerator`
   - `AzureTerraformGenerator`
   - `GcpTerraformGenerator`
4. Add cloud-specific templates and modules.
5. Add service selection logic in `TerraformGenerationService`.

---

## Logging

Logs are stored at:

```
/logs/app.log
```

Includes:
- Incoming requests
- Validation failures
- Template rendering
- ZIP generation
