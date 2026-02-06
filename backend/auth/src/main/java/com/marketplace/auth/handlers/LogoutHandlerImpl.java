package com.marketplace.auth.handlers;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import com.marketplace.auth.entities.Token;
import com.marketplace.auth.repositories.TokenRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Component
public class LogoutHandlerImpl implements LogoutHandler {

  private static final String BEARER_PREFIX = "Bearer ";
  private static final int BEARER_PREFIX_LENGTH = BEARER_PREFIX.length();

  private final TokenRepository tokenRepository;

  @Override
  public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
    String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

    if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
      return;
    }

    String refreshToken = authHeader.substring(BEARER_PREFIX_LENGTH);

    Token tokenEntity = tokenRepository.findByRefreshToken(refreshToken).orElse(null);

    if (tokenEntity != null) {
      tokenRepository.delete(tokenEntity);
    }

    SecurityContextHolder.clearContext();
  }

}