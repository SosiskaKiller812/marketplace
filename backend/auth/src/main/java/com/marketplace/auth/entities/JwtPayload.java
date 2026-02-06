package com.marketplace.auth.entities;

import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public final class JwtPayload {
  private long id;

  private List<String> roles;

  private String type;

  private String issuer;

  private Date issuedAt;

  private Date expiration;

}