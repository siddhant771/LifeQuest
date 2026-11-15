package com.lifequest.controller;
import com.lifequest.repository.UserRepository;import com.lifequest.service.StreakService;import org.springframework.security.core.Authentication;import org.springframework.web.bind.annotation.*;import java.time.*;import java.util.*;
@RestController @RequestMapping("/api/profile") public class ProfileController {
 private final UserRepository users;private final StreakService streaks;public ProfileController(UserRepository u,StreakService s){users=u;streaks=s;}
 @GetMapping public Map<String,Object> get(Authentication a){var u=users.findById(Long.valueOf(a.getName())).orElseThrow(()->new IllegalArgumentException("User not found"));return Map.of("userId",u.getId(),"name",u.getName(),"email",u.getEmail(),"totalXp",u.getTotalXp(),"currentStreak",streaks.currentStreak(u,LocalDate.now(ZoneOffset.UTC)),"longestStreak",u.getLongestStreak());}
}
