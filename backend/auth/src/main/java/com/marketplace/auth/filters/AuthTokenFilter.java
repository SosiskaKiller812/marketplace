package com.marketplace.auth.filters;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.marketplace.auth.configurations.GatewayProperty;
import com.marketplace.auth.entities.JwtPayload;
import com.marketplace.auth.services.JwtService;
import com.marketplace.auth.services.UserDetailsServiceImpl;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {

  private static final String BEARER_PREFIX = "Bearer ";
  private static final int BEARER_PREFIX_LENGTH = BEARER_PREFIX.length();

  private final JwtService jwtService;
  private final UserDetailsServiceImpl userDetailsService;
  private final List<String> publicPaths;

  public AuthTokenFilter(JwtService jwtService, UserDetailsServiceImpl userDetailsServiceImpl,
      GatewayProperty properties) {
    this.jwtService = jwtService;
    this.userDetailsService = userDetailsServiceImpl;
    this.publicPaths = properties.getPublicPaths();
  }

  @Override
  protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {
    try {
      String path = request.getRequestURI();

      if (publicPaths.stream().anyMatch(p -> path.startsWith(p))) {
        filterChain.doFilter(request, response);
        return;
      }

      String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

      if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
        filterChain.doFilter(request, response);
        return;
      }

      String jwt = authHeader.substring(BEARER_PREFIX_LENGTH);

      JwtPayload payload = jwtService.parse(jwt);

      jwtService.validateAccessToken(payload);

      if (SecurityContextHolder.getContext().getAuthentication() == null) {
        UserDetails userDetails = userDetailsService.loadUserById(payload.getId());

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.getAuthorities());

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);

      }
    } catch (Exception e) {
      throw e;
    }

    filterChain.doFilter(request, response);
  }
}
