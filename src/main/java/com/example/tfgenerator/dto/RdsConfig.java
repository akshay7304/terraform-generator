package com.example.tfgenerator.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RdsConfig {
    private Boolean enabled;
    private String engine;
    private String instanceClass;
    private String dbName;
    private String username;
    private String password;
}
