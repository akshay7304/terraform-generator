package com.example.tfgenerator.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean;

import com.example.tfgenerator.dto.EcsConfig;
import com.example.tfgenerator.dto.EnvironmentRequest;
import com.example.tfgenerator.dto.RdsConfig;
import com.example.tfgenerator.dto.Services;
import com.example.tfgenerator.model.TerraformProject;

import freemarker.template.Configuration;

public class TerraformGenerationServiceTest {

    private TerraformGenerationService generationService;

    @BeforeEach
    public void setUp() throws Exception {
        FreeMarkerConfigurationFactoryBean factoryBean =
                new FreeMarkerConfigurationFactoryBean();
        factoryBean.setTemplateLoaderPath("classpath:/templates");
        factoryBean.afterPropertiesSet();
        Configuration config = factoryBean.getObject();

        TerraformTemplateService templateService = new TerraformTemplateService(config);
        generationService = new TerraformGenerationService(templateService);
    }

    @Test
    public void testGenerateWithAllServices() {
        EnvironmentRequest request = createFullRequest();
        TerraformProject project = generationService.generate(request);
        assertNotNull(project);
        assertNotNull(project.getMainTf());
        assertNotNull(project.getVariablesTf());
        assertNotNull(project.getVpcTf());
    }

    private EnvironmentRequest createFullRequest() {
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

        EcsConfig ecs = new EcsConfig();
        ecs.setEnabled(true);
        services.setEcsCluster(ecs);

        request.setServices(services);

        Map<String, String> tags = new HashMap<>();
        tags.put("env", "dev");
        tags.put("owner", "platform-team");
        request.setTags(tags);

        return request;
    }
}
