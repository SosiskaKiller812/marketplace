package com.marketplace.auth.exceptions;

public class CustomJwtException extends RuntimeException {
  public CustomJwtException() {
    super();
  }

  public CustomJwtException(String text) {
    super(text);
  }
}
