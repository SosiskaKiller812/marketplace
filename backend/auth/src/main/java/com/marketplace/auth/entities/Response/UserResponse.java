package com.marketplace.auth.entities.Response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponse {
  private Long id;
  private String username;
  private String email;
  private List<String> roles;
}
