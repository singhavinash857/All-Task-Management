package com.transformedge.gstr.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "audit-configuration")
@Configuration
public class AuditConfiguration {
    private String url;
}
