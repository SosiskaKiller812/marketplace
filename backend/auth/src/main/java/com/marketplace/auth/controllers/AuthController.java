package com.marketplace.auth.controllers;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.marketplace.auth.services.AuthService;
import com.marketplace.auth.entities.Request.LoginRequest;
import com.marketplace.auth.entities.Request.RegisterRequest;
import com.marketplace.auth.entities.Response.UserResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;

@AllArgsConstructor
@RequestMapping("/auth")
@RestController
public class AuthController {
  private final AuthService authService;

  @PostMapping("/signup")
  public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {

    UserResponse userResponse = authService.register(registerRequest);

    return ResponseEntity.status(200).body(userResponse);
  }

  @PostMapping("/signin")
  public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
    return ResponseEntity.ok(authService.login(loginRequest));
  }

  @PostMapping("/refresh")
  public ResponseEntity<?> refreshToken(
      HttpServletRequest request,
      HttpServletResponse response) {

    String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

    if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) { 
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect authorization header");
    }

    String refreshToken = authorizationHeader.substring(7);
    return ResponseEntity.status(HttpStatus.OK).body(authService.refreshToken(refreshToken));
  }

  @GetMapping("/hello")
  public ResponseEntity<String> hello() {
    return ResponseEntity.ok("Hello User");
  }

  @GetMapping("/admin")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<String> helloA() {
    return ResponseEntity.ok("Hello Admin");
  }

}
