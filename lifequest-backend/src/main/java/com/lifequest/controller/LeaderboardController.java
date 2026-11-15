package com.lifequest.controller;
import com.lifequest.dto.LeaderboardEntry;import com.lifequest.service.LeaderboardService;import org.springframework.web.bind.annotation.*;import java.util.*;
@RestController @RequestMapping("/api/leaderboard") public class LeaderboardController {private final LeaderboardService service;public LeaderboardController(LeaderboardService s){service=s;}@GetMapping("/weekly") public List<LeaderboardEntry> weekly(){return service.weekly();}}
