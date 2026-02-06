package com.marketplace.auth.entities.Response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthenticationResponse {

  private String accessToken;

  private String refreshToken;
}
