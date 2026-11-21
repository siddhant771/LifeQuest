package com.lifequest.controller;
import com.lifequest.dto.AuthDtos.*;import com.lifequest.service.AuthService;import jakarta.validation.Valid;import org.springframework.http.*;import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/auth") public class AuthController {
 private final AuthService auth;public AuthController(AuthService a){auth=a;}
 @PostMapping("/register") public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest r){return ResponseEntity.status(HttpStatus.CREATED).body(auth.register(r));}
 @PostMapping("/login") public AuthResponse login(@Valid @RequestBody LoginRequest r){return auth.login(r);}
}
