package com.example.tfgenerator.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.tfgenerator.dto.EnvironmentRequest;
import com.example.tfgenerator.model.TerraformResponse;
import com.example.tfgenerator.util.CidrUtil;

@Service
public class TerraformGenerationService {


    private static final Logger LOGGER = LoggerFactory.getLogger(TerraformGenerationService.class);

    private final TerraformTemplateService templateService;

    public TerraformGenerationService(TerraformTemplateService templateService) {
        this.templateService = templateService;
    }

    public TerraformResponse generate(EnvironmentRequest request) {
    	LOGGER.info("Start generating Terraform project for: {}", request.getName());
        Map<String,Object> model = prepareDataModel(request);
        TerraformResponse response = new TerraformResponse();

        LOGGER.info("Generating Terraform files for {}", request.getName());

        response.setMainTf(templateService.render("main.tf.ftl", model));
        response.setVariablesTf(templateService.render("variables.tf.ftl", model));
        response.setVpcTf(templateService.render("vpc.tf.ftl", model));
        response.setOutputsTf(templateService.render("outputs.tf.ftl", model));
        response.setTerraformTfvars(templateService.render("terraform.tfvars.ftl", model));

        if (Boolean.TRUE.equals(request.getServices() != null ? request.getServices().getS3Bucket() : false)) {
            response.setServicesS3Tf(templateService.render("services_s3.tf.ftl", model));
        } else {
            response.setServicesS3Tf("");
        }

        if (request.getServices() != null && request.getServices().getRds() != null && Boolean.TRUE.equals(request.getServices().getRds().getEnabled())) {
            response.setServicesRdsTf(templateService.render("services_rds.tf.ftl", model));
        } else {
            response.setServicesRdsTf("");
        }

        if (request.getServices() != null && request.getServices().getEcsCluster() != null && Boolean.TRUE.equals(request.getServices().getEcsCluster().getEnabled())) {
            response.setServicesEcsTf(templateService.render("services_ecs.tf.ftl", model));
        } else {
            response.setServicesEcsTf("");
        }

        LOGGER.debug("Terraform generation complete for {}", request.getName());
        LOGGER.info("Finished generating Terraform project for: {}", request.getName());
        return response;
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
    
    public byte[] generateZip(TerraformResponse response) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);

        try {
			addToZip(zos, "main.tf", response.getMainTf());
			addToZip(zos, "variables.tf", response.getVariablesTf());
	        addToZip(zos, "vpc.tf", response.getVpcTf());
	        addToZip(zos, "services_s3.tf", response.getServicesS3Tf());
	        addToZip(zos, "services_rds.tf", response.getServicesRdsTf());
	        addToZip(zos, "services_ecs.tf", response.getServicesEcsTf());
	        addToZip(zos, "outputs.tf", response.getOutputsTf());
	        addToZip(zos, "terraform.tfvars", response.getTerraformTfvars());
	        zos.close();
	        return baos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException("Failed to generate ZIP file", e);
		}
    }

    private void addToZip(ZipOutputStream zos, String fileName, String content){
        if (content == null || content.isBlank()) return;

        ZipEntry entry = new ZipEntry(fileName);
        try {
			zos.putNextEntry(entry);
			zos.write(content.getBytes());
	        zos.closeEntry();
		} catch (IOException e) {
			throw new RuntimeException("Failed writing " + fileName + " to ZIP", e);
		}
        
    }
    
    public TerraformResponse generateTerraformProject(EnvironmentRequest request) {
    	Map<String,Object> model = prepareDataModel(request);
        TerraformResponse response = new TerraformResponse();
        response.setMainTf(templateService.render("main.tf.ftl", model));
        response.setVariablesTf(templateService.render("variables.tf.ftl", model));
        response.setVpcTf(templateService.render("vpc.tf.ftl", model));
        response.setServicesS3Tf(templateService.render("services_s3.tf.ftl", model));
        response.setServicesRdsTf(templateService.render("services_rds.tf.ftl", model));
        response.setServicesEcsTf(templateService.render("services_ecs.tf.ftl", model));
        response.setOutputsTf(templateService.render("outputs.tf.ftl", model));
        response.setTerraformTfvars(templateService.render("terraform.tfvars.ftl", model));

        return response;
    }


}
