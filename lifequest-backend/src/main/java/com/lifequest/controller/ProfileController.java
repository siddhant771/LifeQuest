package com.lifequest.controller;
import com.lifequest.dto.ProfileResponse;import com.lifequest.repository.UserRepository;import com.lifequest.service.StreakService;import org.springframework.security.core.Authentication;import org.springframework.web.bind.annotation.*;import java.time.*;import java.util.*;
@RestController @RequestMapping("/api/profile") public class ProfileController {
 private final UserRepository users;private final StreakService streaks;public ProfileController(UserRepository u,StreakService s){users=u;streaks=s;}
 @GetMapping public ProfileResponse get(Authentication a){var u=users.findById(Long.valueOf(a.getName())).orElseThrow(()->new IllegalArgumentException("User not found"));return new ProfileResponse(u.getId(),u.getName(),u.getEmail(),u.getTotalXp(),streaks.currentStreak(u,LocalDate.now(ZoneOffset.UTC)),u.getLongestStreak());}
}
