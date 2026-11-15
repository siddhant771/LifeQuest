package com.lifequest.service;
import com.lifequest.dto.AuthDtos.RegisterRequest;import com.lifequest.entity.User;import com.lifequest.repository.UserRepository;import com.lifequest.security.JwtService;import org.junit.jupiter.api.Test;import org.junit.jupiter.api.extension.ExtendWith;import org.mockito.*;import org.mockito.junit.jupiter.MockitoExtension;import org.springframework.security.crypto.password.PasswordEncoder;import static org.junit.jupiter.api.Assertions.*;import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class) class AuthServiceTest {
 @Mock UserRepository users;@Mock PasswordEncoder encoder;@Mock JwtService jwt;@InjectMocks AuthService service;
 @Test void registrationStoresHashedPasswordAndNormalizedEmail(){
  when(users.existsByEmailIgnoreCase("user@example.com")).thenReturn(false);when(encoder.encode("long-password")).thenReturn("bcrypt-hash");
  service.register(new RegisterRequest(" Test User ","USER@EXAMPLE.COM","long-password"));
  ArgumentCaptor<User> captor=ArgumentCaptor.forClass(User.class);verify(users).save(captor.capture());
  assertEquals("user@example.com",captor.getValue().getEmail());assertEquals("bcrypt-hash",captor.getValue().getPasswordHash());
 }
 @Test void duplicateEmailIsRejected(){when(users.existsByEmailIgnoreCase("user@example.com")).thenReturn(true);assertThrows(IllegalArgumentException.class,()->service.register(new RegisterRequest("User","user@example.com","long-password")));verify(users,never()).save(any());}
}
