package com.marketplace.gateway.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Component
@ConfigurationProperties(prefix = "spring.cloud.gateway")
public class GatewayProperty {
  private List<String> publicPaths;
}
