package com.marketplace.auth.services;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import com.marketplace.auth.entities.JwtPayload;
import com.marketplace.auth.entities.impl.UserDetailsImpl;
import com.marketplace.auth.exceptions.CustomJwtException;
import com.marketplace.auth.parsers.RSAKeyParser;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class JwtService {

  private static final String ROLES_CLAIM = "roles";
  private static final String TYPE_CLAIM = "typ";
  private static final String ACCESS_TYPE = "ACCESS";
  private static final String REFRESH_TYPE = "REFRESH";
  private static final String SERVICE_NAME = "AUTH-SERVICE";

  private final RSAKeyParser keyParser;

  @Value("${jwt.refresh.expiration}")
  private Long refreshExpiration;

  @Value("${jwt.access.expiration}")
  private Long accessExpiration;

  @Value("${jwt.private-path}")
  private Resource privateKeyResource;

  @Value("${jwt.public-path}")
  private Resource publicKeyResource;

  private RSAPrivateKey privateKey;

  private RSAPublicKey publicKey;

  public JwtService(RSAKeyParser rsaKeyParser) {
    this.keyParser = rsaKeyParser;
  }

  @PostConstruct
  public void init() throws Exception {
    privateKey = keyParser.loadPrivate(privateKeyResource);
    publicKey = keyParser.loadPublic(publicKeyResource);
  }

  public String generateAccessToken(UserDetailsImpl userDetails) {
    List<String> roles = userDetails.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.toList());

    return Jwts.builder()
        .subject(String.valueOf(userDetails.getId()))
        .claim(ROLES_CLAIM, roles)
        .claim(TYPE_CLAIM, ACCESS_TYPE)
        .issuer(SERVICE_NAME)
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + accessExpiration))
        .signWith(privateKey, Jwts.SIG.RS256)
        .compact();
  }

  public String generateRefreshToken(UserDetailsImpl userDetails) {
    return Jwts.builder()
        .subject(String.valueOf(userDetails.getId()))
        .claim(TYPE_CLAIM, REFRESH_TYPE)
        .issuer(SERVICE_NAME)
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + refreshExpiration))
        .signWith(privateKey, Jwts.SIG.RS256)
        .compact();
  }

  public JwtPayload parse(String token) {
    try {
      Claims claims = Jwts.parser()
          .verifyWith(publicKey)
          .build()
          .parseSignedClaims(token)
          .getPayload();

      return JwtPayload.builder()
          .id(Long.parseLong(claims.getSubject()))
          .roles(getRolesFromClaims(claims))
          .type(claims.get(TYPE_CLAIM, String.class))
          .issuer(claims.getIssuer())
          .issuedAt(claims.getIssuedAt())
          .expiration(claims.getExpiration())
          .build();
    } catch (ExpiredJwtException e) {
      throw new CustomJwtException("JWT expired");
    } catch (Exception e) {
      throw new CustomJwtException("JWT invalid");
    }
  }

  private List<String> getRolesFromClaims(Claims claims) {
    Object roles = claims.get(ROLES_CLAIM);
    if (roles instanceof List) {
      return ((List<?>) roles).stream()
          .filter(String.class::isInstance)
          .map(String.class::cast)
          .toList();
    }
    return Collections.emptyList();
  }

  public void validateAccessToken(JwtPayload payload) {
    if (!ACCESS_TYPE.equals(payload.getType())) {
      log.info(ACCESS_TYPE + "==" + payload.getType());
      throw new CustomJwtException("Invalid token type");
    }

    if (!SERVICE_NAME.equals(payload.getIssuer())) {
      throw new CustomJwtException("Invalid token issuer");
    }

    if (payload.getExpiration().before(new Date())) {
      throw new CustomJwtException("Token expired");
    }
  }

  public void validateRefreshToken(JwtPayload payload) {
    if (!REFRESH_TYPE.equals(payload.getType())) {
      throw new CustomJwtException("Invalid token type");
    }

    if (!SERVICE_NAME.equals(payload.getIssuer())) {
      throw new CustomJwtException("Invalid token issuer");
    }

    if (payload.getExpiration().before(new Date())) {
      throw new CustomJwtException("Token expired");
    }
  }
}
