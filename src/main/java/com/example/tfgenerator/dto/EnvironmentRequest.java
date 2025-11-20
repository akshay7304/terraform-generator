package com.example.tfgenerator.dto;

import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EnvironmentRequest {
    private String name;
    private String region;
    private String vpcCidr;
    private Services services;
    private Map<String, String> tags;
}
