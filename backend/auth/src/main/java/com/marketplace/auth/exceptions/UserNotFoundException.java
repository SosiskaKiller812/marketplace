package com.marketplace.auth.exceptions;

public class UserNotFoundException extends RuntimeException {

  public UserNotFoundException(String name) {
    super(new String("Username with name " + '\'' + name + "\' not found"));
  }
}
