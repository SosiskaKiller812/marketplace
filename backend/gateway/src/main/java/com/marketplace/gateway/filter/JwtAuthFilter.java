package com.marketplace.gateway.filter;

import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.marketplace.gateway.config.GatewayProperty;
import com.marketplace.gateway.parser.RSAKeyParser;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

  private static final String ROLES_CLAIM = "roles";
  private static final String TYPE_CLAIM = "typ";
  private static final String ACCESS_TYPE = "ACCESS";
  private static final String BEARER = "Bearer ";
  private static final String ISSUER = "AUTH-SERVICE";

  private final List<String> publicPaths;

  private final RSAKeyParser keyParser;

  private final Resource publicKeyResource;

  private RSAPublicKey publicKey;

  public JwtAuthFilter(
      RSAKeyParser keyParser,
      GatewayProperty properties,
      @Value("${jwt.public-path}") Resource publicKeyResource) {
    this.keyParser = keyParser;
    this.publicPaths = properties.getPublicPaths();
    this.publicKeyResource = publicKeyResource;
  }

  @PostConstruct
  public void init() {
    try {
      publicKey = keyParser.loadPublic(publicKeyResource);
    } catch (Exception exc) {
      throw new RuntimeException("Public key not found");
    }
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    String path = exchange.getRequest().getPath().value();
    // log.info("CURRENT PATH: " + path);
    // for (String pathS : publicPaths) {
    //   log.info("'" + pathS + "'\n");
    // }

    if (publicPaths.stream().anyMatch(p -> path.startsWith(p))) {
      log.info("public path");
      return chain.filter(exchange);
    }

    // log.info("ne public path");

    String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

    if (authHeader == null || !authHeader.startsWith(BEARER)) {
      exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
      return exchange.getResponse().setComplete();
    }

    String token = authHeader.substring(7);

    try {
      Claims claims = Jwts.parser()
          .verifyWith(publicKey)
          .requireIssuer(ISSUER)
          .build()
          .parseSignedClaims(token)
          .getPayload();

      // log.info("\n----------------------------\nverified\n");

      validate(claims);

      // log.info("\n-----------------------------\nvalidirovan");

      String userId = claims.getSubject();
      String roles = extractRoles(claims);

      ServerHttpRequest request = exchange.getRequest().mutate()
          .header("X-User-Id", userId)
          .header("X-User-Roles", roles)
          .build();

      return chain.filter(exchange.mutate().request(request).build());
    } catch (JwtException e) {
      exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
      return exchange.getResponse().setComplete();
    }
  }

  private void validate(Claims claims) {
    if (!ACCESS_TYPE.equals(claims.get(TYPE_CLAIM, String.class))) {
      throw new JwtException("Invalid token type");
    }

    if (claims.getSubject() == null) {
      throw new JwtException("No subject");
    }

    if (claims.getExpiration().before(new Date())) {
      throw new JwtException("Token expired");
    }
  }

  private String extractRoles(Claims claims) {
    Object roles = claims.get(ROLES_CLAIM);

    if (roles instanceof List<?>) {
      return ((List<?>) roles).stream()
          .filter(String.class::isInstance)
          .map(String.class::cast)
          .collect(Collectors.joining(","));
    }

    return "";
  }

  @Override
  public int getOrder() {
    return -1;
  }
}
