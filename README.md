# Terraform Generator

A small Spring Boot service that generates Terraform code for a basic AWS environment from a JSON request.

## Quick start

1. Build
```
mvn clean install
```

2. Run
```
mvn spring-boot:run
```

3. API
POST `http://localhost:8080/api/v1/environments`
Content-Type: application/json

Sample request is provided in `sample-request.json`.

## Notes
- This service **does not** apply any Terraform; it only generates files.
- Keep your AWS credentials ready if you want to run `terraform plan` against the generated files.
