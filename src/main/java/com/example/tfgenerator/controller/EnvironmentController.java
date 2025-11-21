package com.example.tfgenerator.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.tfgenerator.dto.ApiResponse;
import com.example.tfgenerator.dto.EnvironmentRequest;
import com.example.tfgenerator.model.TerraformResponse;
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
    public ResponseEntity<ApiResponse<TerraformResponse>> generateEnvironment(@RequestBody EnvironmentRequest request) {
        LOGGER.info("Received generate environment request: name={}, region={}", request.getName(), request.getRegion());
        validator.validate(request);
        TerraformResponse response = generationService.generate(request);
        LOGGER.debug("Generated terraform project with files: main_tf length={}, vpc_tf length={}", 
                response.getMainTf() != null ? response.getMainTf().length() : 0,
                response.getVpcTf() != null ? response.getVpcTf().length() : 0);
        return ResponseEntity.ok(new ApiResponse<TerraformResponse>(true, response, null));
    }
    
    @PostMapping(value = "/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> downloadTerraform(@RequestBody EnvironmentRequest request) {

        validator.validate(request);

        TerraformResponse response = generationService.generateTerraformProject(request);
        byte[] zipBytes = generationService.generateZip(response);

        String fileName = request.getName() + ".zip";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", fileName);
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentLength(zipBytes.length);

        return new ResponseEntity<>(zipBytes, headers, HttpStatus.OK);
    }
}
