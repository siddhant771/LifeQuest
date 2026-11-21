package com.lifequest.controller;
import com.lifequest.dto.AchievementResponse;import com.lifequest.service.AchievementService;import org.springframework.security.core.Authentication;import org.springframework.web.bind.annotation.*;import java.util.List;
@RestController @RequestMapping("/api/achievements") public class AchievementController {
 private final AchievementService service;public AchievementController(AchievementService s){service=s;}
 @GetMapping public List<AchievementResponse> list(Authentication a){return service.list(Long.valueOf(a.getName())).stream().map(x->new AchievementResponse(x.getId(),x.getCode(),x.getUnlockedAt())).toList();}
}
