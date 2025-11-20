package com.example.tfgenerator.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.tfgenerator.dto.ApiResponse;
import com.example.tfgenerator.dto.EnvironmentRequest;
import com.example.tfgenerator.model.TerraformProject;
import com.example.tfgenerator.service.TerraformGenerationService;
import com.example.tfgenerator.validator.EnvironmentValidator;

@RestController
@RequestMapping("/api/v1")
public class EnvironmentController {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnvironmentController.class);

    private final EnvironmentValidator validator;
    private final TerraformGenerationService generationService;

    public EnvironmentController(EnvironmentValidator validator, TerraformGenerationService generationService) {
        this.validator = validator;
        this.generationService = generationService;
    }

    @PostMapping("/environments")
    public ResponseEntity<ApiResponse<TerraformProject>> generateEnvironment(@RequestBody EnvironmentRequest request) {
        LOGGER.info("Received generate environment request: name={}, region={}", request.getName(), request.getRegion());
        validator.validate(request);
        TerraformProject project = generationService.generate(request);
        LOGGER.debug("Generated terraform project with files: main_tf length={}, vpc_tf length={}", 
                project.getMainTf() != null ? project.getMainTf().length() : 0,
                project.getVpcTf() != null ? project.getVpcTf().length() : 0);
        return ResponseEntity.ok(new ApiResponse<TerraformProject>(true, project, null));
    }
}
