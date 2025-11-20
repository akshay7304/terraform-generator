package com.example.tfgenerator.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TerraformResponse {

    @JsonProperty("main_tf")
    private String mainTf;

    @JsonProperty("variables_tf")
    private String variablesTf;

    @JsonProperty("vpc_tf")
    private String vpcTf;

    @JsonProperty("services_s3_tf")
    private String servicesS3Tf;

    @JsonProperty("services_rds_tf")
    private String servicesRdsTf;

    @JsonProperty("services_ecs_tf")
    private String servicesEcsTf;

    @JsonProperty("outputs_tf")
    private String outputsTf;

    @JsonProperty("terraform_tfvars")
    private String terraformTfvars;
}
