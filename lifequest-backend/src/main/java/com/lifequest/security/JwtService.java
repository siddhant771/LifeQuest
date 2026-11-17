package com.lifequest.security;
import io.jsonwebtoken.Jwts;import io.jsonwebtoken.security.Keys;import jakarta.annotation.PostConstruct;import org.springframework.beans.factory.annotation.Value;import org.springframework.stereotype.Service;import javax.crypto.SecretKey;import java.nio.charset.StandardCharsets;import java.util.Date;
@Service public class JwtService {
 @Value("${lifequest.jwt.secret}") private String secret;@Value("${lifequest.jwt.expiration-ms:86400000}") private long expirationMs;private SecretKey key;
 @PostConstruct void init(){if(secret==null||secret.getBytes(StandardCharsets.UTF_8).length<32)throw new IllegalStateException("JWT_SECRET must contain at least 32 bytes");if(expirationMs<=0)throw new IllegalStateException("JWT_EXPIRATION_MS must be positive");key=Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));}
 public String issue(Long id,String email){long now=System.currentTimeMillis();return Jwts.builder().subject(id.toString()).claim("email",email).issuedAt(new Date(now)).expiration(new Date(now+expirationMs)).signWith(key).compact();}
 public Long parseUserId(String token){return Long.valueOf(Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject());}
}
