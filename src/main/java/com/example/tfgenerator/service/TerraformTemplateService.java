package com.example.tfgenerator.service;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

@Service
public class TerraformTemplateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TerraformTemplateService.class);

    private final Configuration freemarkerConfig;

    public TerraformTemplateService(Configuration freemarkerConfig) {
        this.freemarkerConfig = freemarkerConfig;
    }

    public String render(String templateName, Map<String, Object> dataModel) {
        try {
            LOGGER.debug("Rendering template {} with model keys={}", templateName, dataModel != null ? dataModel.keySet() : null);
            Template template = freemarkerConfig.getTemplate("terraform/" + templateName);
            StringWriter writer = new StringWriter();
            template.process(dataModel, writer);
            return writer.toString();
        } catch (IOException | TemplateException e) {
            LOGGER.error("Failed to render template {}", templateName, e);
            throw new RuntimeException("Failed to render template: " + templateName, e);
        }
    }
}
