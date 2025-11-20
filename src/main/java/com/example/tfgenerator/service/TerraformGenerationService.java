package com.example.tfgenerator.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.tfgenerator.dto.EnvironmentRequest;
import com.example.tfgenerator.model.TerraformProject;
import com.example.tfgenerator.util.CidrUtil;

@Service
public class TerraformGenerationService {


    private static final Logger LOGGER = LoggerFactory.getLogger(TerraformGenerationService.class);

    private final TerraformTemplateService templateService;

    public TerraformGenerationService(TerraformTemplateService templateService) {
        this.templateService = templateService;
    }

    public TerraformProject generate(EnvironmentRequest request) {
    	LOGGER.info("Start generating Terraform project for: {}", request.getName());
        Map<String,Object> model = prepareDataModel(request);
        TerraformProject project = new TerraformProject();

        LOGGER.info("Generating Terraform files for {}", request.getName());

        project.setMainTf(templateService.render("main.tf.ftl", model));
        project.setVariablesTf(templateService.render("variables.tf.ftl", model));
        project.setVpcTf(templateService.render("vpc.tf.ftl", model));
        project.setOutputsTf(templateService.render("outputs.tf.ftl", model));
        project.setTerraformTfvars(templateService.render("terraform.tfvars.ftl", model));

        if (Boolean.TRUE.equals(request.getServices() != null ? request.getServices().getS3Bucket() : false)) {
            project.setServicesS3Tf(templateService.render("services_s3.tf.ftl", model));
        } else {
            project.setServicesS3Tf("");
        }

        if (request.getServices() != null && request.getServices().getRds() != null && Boolean.TRUE.equals(request.getServices().getRds().getEnabled())) {
            project.setServicesRdsTf(templateService.render("services_rds.tf.ftl", model));
        } else {
            project.setServicesRdsTf("");
        }

        if (request.getServices() != null && request.getServices().getEcsCluster() != null && Boolean.TRUE.equals(request.getServices().getEcsCluster().getEnabled())) {
            project.setServicesEcsTf(templateService.render("services_ecs.tf.ftl", model));
        } else {
            project.setServicesEcsTf("");
        }

        LOGGER.debug("Terraform generation complete for {}", request.getName());
        LOGGER.info("Finished generating Terraform project for: {}", request.getName());
        return project;
    }

    private Map<String,Object> prepareDataModel(EnvironmentRequest request) {
        Map<String,Object> model = new HashMap<>();
        model.put("name", request.getName());
        model.put("region", request.getRegion());
        model.put("vpcCidr", request.getVpcCidr());

        List<String> subnets = CidrUtil.splitCidr(request.getVpcCidr(), 2);
        model.put("publicSubnetCidr", subnets.get(0));
        model.put("privateSubnetCidr", subnets.get(1));

        model.put("s3Enabled", request.getServices() != null && Boolean.TRUE.equals(request.getServices().getS3Bucket()));

        if (request.getServices() != null && request.getServices().getRds() != null && Boolean.TRUE.equals(request.getServices().getRds().getEnabled())) {
            model.put("rdsEnabled", true);
            model.put("rdsEngine", request.getServices().getRds().getEngine());
            model.put("rdsInstanceClass", request.getServices().getRds().getInstanceClass());
            model.put("rdsDbName", request.getServices().getRds().getDbName());
            model.put("rdsUsername", request.getServices().getRds().getUsername());
            model.put("rdsPassword", request.getServices().getRds().getPassword());
        } else {
            model.put("rdsEnabled", false);
        }

        model.put("ecsEnabled", request.getServices() != null && request.getServices().getEcsCluster() != null && Boolean.TRUE.equals(request.getServices().getEcsCluster().getEnabled()));

        model.put("tags", request.getTags() != null ? request.getTags() : new HashMap<String,String>());

        return model;
    }
}
