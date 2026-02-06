package com.marketplace.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import lombok.NoArgsConstructor;

@NoArgsConstructor
@Configuration
public class GatewayConfig {

  @Bean
  public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
    return http
        .csrf(ServerHttpSecurity.CsrfSpec::disable)

        .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
        .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)

        .authorizeExchange(exchanges -> exchanges
            // .pathMatchers(org.springframework.http.HttpMethod.OPTIONS).permitAll()
            .anyExchange().permitAll())
        .build();
  }
}
