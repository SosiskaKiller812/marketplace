package com.marketplace.auth.services;

import java.util.Set;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.marketplace.auth.entities.JwtPayload;
import com.marketplace.auth.entities.Role;
import com.marketplace.auth.entities.Token;
import com.marketplace.auth.entities.User;
import com.marketplace.auth.entities.Request.LoginRequest;
import com.marketplace.auth.entities.Request.RegisterRequest;
import com.marketplace.auth.entities.Response.AuthenticationResponse;
import com.marketplace.auth.entities.Response.UserResponse;
import com.marketplace.auth.entities.impl.UserDetailsImpl;
import com.marketplace.auth.mappers.UserMapper;
import com.marketplace.auth.repositories.RoleRepository;
import com.marketplace.auth.repositories.TokenRepository;
import com.marketplace.auth.repositories.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@Service
public class AuthService {

  private static final String ROLE_USER = "ROLE_USER";

  private final TokenRepository tokenRepository;

  private final UserRepository userRepository;

  private final RoleRepository roleRepository;

  private final AuthenticationManager authenticationManager;

  private final JwtService jwtService;

  private final PasswordEncoder passwordEncoder;

  private final UserMapper userMapper;

  @Transactional
  public UserResponse register(RegisterRequest registerRequest) {

    Role role = roleRepository.findByName(ROLE_USER).orElseThrow();

    User newUser = new User(
        registerRequest.getUsername(),
        registerRequest.getEmail(),
        passwordEncoder.encode(registerRequest.getPassword()),
        Set.of(role));

    if (userRepository.existsByUsername(registerRequest.getUsername())) {
      throw new RuntimeException("Username already exists");
    }

    if (userRepository.existsByEmail(registerRequest.getEmail())) {
      throw new RuntimeException("This email already in use");
    }

    userRepository.save(newUser);

    return userMapper.toUserResponse(newUser);
  }

  @Transactional
  public AuthenticationResponse login(LoginRequest loginRequest) {
    Authentication auth = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

    UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();

    User user = userRepository.findById(userDetails.getId()).orElseThrow();

    String accessToken = jwtService.generateAccessToken(userDetails);
    String refreshToken = jwtService.generateRefreshToken(userDetails);

    Token token = new Token(refreshToken, user);
    tokenRepository.save(token);

    return new AuthenticationResponse(accessToken, refreshToken);
  }

  @Transactional
  public AuthenticationResponse refreshToken(String incomingRefreshToken) {
    JwtPayload payload = jwtService.parse(incomingRefreshToken);

    jwtService.validateRefreshToken(payload);

    User user = userRepository.findById(payload.getId())
        .orElseThrow(() -> new RuntimeException("User not found by id"));

    Token existingToken = tokenRepository.findByRefreshToken(incomingRefreshToken)
        .orElseThrow(() -> new RuntimeException("Refresh token not found in database"));

    if (!existingToken.getUser().getId().equals(user.getId())) {
      throw new RuntimeException("Something went wrong with refreshToken and user");
    }

    UserDetailsImpl userDetails = UserDetailsImpl.fromUser(user);

    String newAccessToken = jwtService.generateAccessToken(userDetails);
    String newRefreshToken = jwtService.generateRefreshToken(userDetails);

    tokenRepository.deleteByRefreshToken(incomingRefreshToken);
    tokenRepository.save(new Token(newRefreshToken, user));

    return new AuthenticationResponse(newAccessToken, newRefreshToken);
  }

  @Transactional
  public void addRole(String username, String roleName) {
    Role role = roleRepository.findByName(roleName)
        .orElseThrow(() -> new EntityNotFoundException("Role not found"));
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new EntityNotFoundException("User not found"));
    user.getRoles().add(role);
    userRepository.save(user);
  }

  @Transactional
  public void removeRole(String username, String roleName) {
    Role role = roleRepository.findByName(roleName)
        .orElseThrow(() -> new EntityNotFoundException("Role not found"));
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new EntityNotFoundException("User not found"));
    user.getRoles().add(role);
    userRepository.save(user);
  }
}
