package com.lifequest.service;
import com.lifequest.dto.AuthDtos.*;import com.lifequest.entity.User;import com.lifequest.repository.UserRepository;import com.lifequest.security.JwtService;import org.springframework.security.crypto.password.PasswordEncoder;import org.springframework.stereotype.Service;import org.springframework.transaction.annotation.Transactional;
@Service public class AuthService {
 private final UserRepository users;private final PasswordEncoder encoder;private final JwtService jwt;
 public AuthService(UserRepository u,PasswordEncoder e,JwtService j){users=u;encoder=e;jwt=j;}
 @Transactional public String register(RegisterRequest r){String email=r.email().trim().toLowerCase();if(users.existsByEmailIgnoreCase(email))throw new IllegalArgumentException("Email is already registered");User u=new User();u.setName(r.name().trim());u.setEmail(email);u.setPasswordHash(encoder.encode(r.password()));users.save(u);return "Registration successful";}
 @Transactional(readOnly=true) public AuthResponse login(LoginRequest r){User u=users.findByEmailIgnoreCase(r.email().trim()).orElseThrow(()->new IllegalArgumentException("Invalid email or password"));if(!encoder.matches(r.password(),u.getPasswordHash()))throw new IllegalArgumentException("Invalid email or password");return new AuthResponse(jwt.issue(u.getId(),u.getEmail()),"Bearer",u.getId(),u.getEmail());}
}
