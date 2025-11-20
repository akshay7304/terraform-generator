package com.example.tfgenerator.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Services {
    private Boolean s3Bucket = false;
    private RdsConfig rds;
    private EcsConfig ecsCluster;
}
