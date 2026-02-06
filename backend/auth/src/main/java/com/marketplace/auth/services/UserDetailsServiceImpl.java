package com.marketplace.auth.services;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.marketplace.auth.entities.User;
import com.marketplace.auth.entities.impl.UserDetailsImpl;
import com.marketplace.auth.exceptions.UserNotFoundException;
import com.marketplace.auth.repositories.UserRepository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository.findByUsername(username).orElseThrow(
        () -> new UserNotFoundException(username));

    return UserDetailsImpl.fromUser(user);
  }

  public UserDetails loadUserById(Long id) {
    User user = userRepository.findById(id).orElseThrow(
        () -> new RuntimeException("Id not found"));

    return UserDetailsImpl.fromUser(user);
  }

}
