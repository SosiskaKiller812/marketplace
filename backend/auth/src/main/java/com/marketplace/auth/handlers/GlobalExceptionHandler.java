package com.marketplace.auth.handlers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.marketplace.auth.exceptions.CustomJwtException;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.UnsupportedJwtException;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(CustomJwtException.class)
  public ResponseEntity<?> jwtException(CustomJwtException exc) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exc.getMessage());
  }

  @ExceptionHandler(UnsupportedJwtException.class)
  public ResponseEntity<?> jwtException(UnsupportedJwtException exc) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("JWT token is unsupported:" + exc.getMessage());
  }

  @ExceptionHandler(ExpiredJwtException.class)
  public ResponseEntity<?> jwtException(ExpiredJwtException exc) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("JWT token is expired: " + exc.getMessage());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<?> jwtException(IllegalArgumentException exc) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("JWT claims string is empty:" + exc.getMessage());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<?> jwtException(Exception exc) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Exception: " + exc.getMessage());
  }
}
