package com.marketplace.auth.entities.Request;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginRequest {

  private String username;

  @Column
  private String password;
}
