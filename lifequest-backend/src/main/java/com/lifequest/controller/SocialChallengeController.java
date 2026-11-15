package com.lifequest.controller;
import org.springframework.jdbc.core.JdbcTemplate;import org.springframework.security.core.Authentication;import org.springframework.web.bind.annotation.*;import java.time.*;import java.util.*;
@RestController @RequestMapping("/api/challenges") public class SocialChallengeController {
 private final JdbcTemplate jdbc;public SocialChallengeController(JdbcTemplate j){jdbc=j;}private long uid(Authentication a){return Long.parseLong(a.getName());}
 @PostMapping("/friends") public Map<String,Object> invite(@RequestBody Map<String,Object> body,Authentication a){
  long from=uid(a);long to=Long.parseLong(String.valueOf(body.get("inviteeId")));String title=String.valueOf(body.getOrDefault("title","Friend challenge"));int target=Integer.parseInt(String.valueOf(body.getOrDefault("targetXp","100")));if(from==to||target<1||target>100000)throw new IllegalArgumentException("Invalid challenge");
  OffsetDateTime start=OffsetDateTime.now(ZoneOffset.UTC);OffsetDateTime end=start.plusDays(7);
  Long id=jdbc.queryForObject("INSERT INTO friend_challenges(creator_id,invitee_id,title,target_xp,starts_at,ends_at,status) VALUES(?,?,?,?,?,?, 'PENDING') RETURNING id",Long.class,from,to,title,target,start,end);
  return Map.of("challengeId",id,"status","PENDING");
 }
 @PostMapping("/friends/{id}/accept") public Map<String,Object> accept(@PathVariable long id,Authentication a){
  int changed=jdbc.update("UPDATE friend_challenges SET status='ACCEPTED' WHERE id=? AND invitee_id=? AND status='PENDING'",id,uid(a));
  if(changed==0)throw new IllegalArgumentException("Invitation not found or already handled");return Map.of("challengeId",id,"status","ACCEPTED");
 }
 @PostMapping("/teams") public Map<String,Object> createTeam(@RequestBody Map<String,Object> body,Authentication a){
  long user=uid(a);String title=String.valueOf(body.getOrDefault("title","Weekly league"));int target=Integer.parseInt(String.valueOf(body.getOrDefault("targetXp","500")));if(target<1||target>100000)throw new IllegalArgumentException("Invalid target XP");
  LocalDate week=LocalDate.now(ZoneOffset.UTC).with(java.time.DayOfWeek.MONDAY);
  Long id=jdbc.queryForObject("INSERT INTO team_challenges(creator_id,title,target_xp,week_start) VALUES(?,?,?,?) RETURNING id",Long.class,user,title,target,week);
  jdbc.update("INSERT INTO team_challenge_members(team_challenge_id,user_id) VALUES(?,?)",id,user);return Map.of("teamChallengeId",id,"weekStart",week.toString());
 }
 @PostMapping("/teams/{id}/join") public Map<String,Object> joinTeam(@PathVariable long id,Authentication a){
  int changed=jdbc.update("INSERT INTO team_challenge_members(team_challenge_id,user_id) SELECT id,? FROM team_challenges WHERE id=? ON CONFLICT DO NOTHING",uid(a),id);
  if(changed==0)throw new IllegalArgumentException("Team challenge not found or already joined");return Map.of("teamChallengeId",id,"joined",true);
 }
 @GetMapping("/teams") public List<Map<String,Object>> teams(Authentication a){return jdbc.queryForList("SELECT t.id,t.title,t.target_xp,t.week_start,COUNT(m.user_id) AS member_count FROM team_challenges t JOIN team_challenge_members m ON m.team_challenge_id=t.id WHERE m.user_id=? GROUP BY t.id ORDER BY t.week_start DESC",uid(a));}
}
