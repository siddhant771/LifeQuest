package com.lifequest.controller;
import com.lifequest.repository.UserRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.*;
import java.util.*;
@RestController @RequestMapping("/api/challenges")
public class SocialChallengeController{
 private final JdbcTemplate jdbc;private final UserRepository users;
 public SocialChallengeController(JdbcTemplate jdbc,UserRepository users){this.jdbc=jdbc;this.users=users;}
 private long uid(Authentication a){return Long.parseLong(a.getName());}
 @PostMapping("/friends") public Map<String,Object> invite(@RequestBody Map<String,Object> body,Authentication a){
  long from=uid(a);Object raw=body.get("inviteeId");if(raw==null)throw new IllegalArgumentException("inviteeId is required");long to=Long.parseLong(String.valueOf(raw));
  if(from==to||!users.existsById(to))throw new IllegalArgumentException("Invitee account not found");
  String title=String.valueOf(body.getOrDefault("title","Friend challenge")).trim();int target=Integer.parseInt(String.valueOf(body.getOrDefault("targetXp","100")));
  if(title.isBlank()||title.length()>120||target<1||target>100000)throw new IllegalArgumentException("Invalid challenge details");
  OffsetDateTime start=OffsetDateTime.now(ZoneOffset.UTC),end=start.plusDays(7);
  Long id=jdbc.queryForObject("INSERT INTO friend_challenges(creator_id,invitee_id,title,target_xp,starts_at,ends_at,status) VALUES(?,?,?,?,?,?,'PENDING') RETURNING id",Long.class,from,to,title,target,start,end);
  return Map.of("challengeId",id,"status","PENDING");
 }
 @GetMapping("/friends/invitations") public List<Map<String,Object>> invitations(Authentication a){return jdbc.queryForList("SELECT id,creator_id,title,target_xp,starts_at,ends_at,status FROM friend_challenges WHERE invitee_id=? AND status='PENDING' AND ends_at>now() ORDER BY created_at DESC",uid(a));}
 @PostMapping("/friends/{id}/accept") public Map<String,Object> accept(@PathVariable long id,Authentication a){
  int n=jdbc.update("UPDATE friend_challenges SET status='ACCEPTED' WHERE id=? AND invitee_id=? AND status='PENDING' AND ends_at>now()",id,uid(a));
  if(n==0)throw new IllegalArgumentException("Invitation not found, expired or already handled");return Map.of("challengeId",id,"status","ACCEPTED");
 }
 @GetMapping("/friends/active") public List<Map<String,Object>> activeFriends(Authentication a){
  long user=uid(a);return jdbc.queryForList("""
   SELECT c.id,c.title,c.target_xp,c.starts_at,c.ends_at,c.status,
    c.creator_id,c.invitee_id,
    COALESCE(SUM(CASE WHEN e.user_id=c.creator_id THEN e.xp_awarded ELSE 0 END),0) AS creator_xp,
    COALESCE(SUM(CASE WHEN e.user_id=c.invitee_id THEN e.xp_awarded ELSE 0 END),0) AS invitee_xp
   FROM friend_challenges c
   LEFT JOIN quest_completion_events e ON e.user_id IN (c.creator_id,c.invitee_id)
      AND e.completed_at>=c.starts_at AND e.completed_at<c.ends_at
   WHERE (c.creator_id=? OR c.invitee_id=?) AND c.status='ACCEPTED' AND c.ends_at>now()
   GROUP BY c.id ORDER BY c.ends_at
   """,user,user);
 }
 @PostMapping("/teams") public Map<String,Object> createTeam(@RequestBody Map<String,Object> body,Authentication a){
  long user=uid(a);String title=String.valueOf(body.getOrDefault("title","Weekly league")).trim();int target=Integer.parseInt(String.valueOf(body.getOrDefault("targetXp","500")));
  if(title.isBlank()||title.length()>120||target<1||target>100000)throw new IllegalArgumentException("Invalid team challenge details");
  LocalDate week=LocalDate.now(ZoneOffset.UTC).with(DayOfWeek.MONDAY);
  Long id=jdbc.queryForObject("INSERT INTO team_challenges(creator_id,title,target_xp,week_start) VALUES(?,?,?,?) RETURNING id",Long.class,user,title,target,week);
  jdbc.update("INSERT INTO team_challenge_members(team_challenge_id,user_id) VALUES(?,?)",id,user);return Map.of("teamChallengeId",id,"weekStart",week.toString());
 }
 @PostMapping("/teams/{id}/join") public Map<String,Object> joinTeam(@PathVariable long id,Authentication a){
  int n=jdbc.update("INSERT INTO team_challenge_members(team_challenge_id,user_id) SELECT id,? FROM team_challenges WHERE id=? ON CONFLICT DO NOTHING",uid(a),id);
  if(n==0)throw new IllegalArgumentException("Team challenge not found or already joined");return Map.of("teamChallengeId",id,"joined",true);
 }
 @GetMapping("/teams") public List<Map<String,Object>> teams(Authentication a){
  return jdbc.queryForList("SELECT t.id,t.title,t.target_xp,t.week_start,COUNT(m.user_id) AS member_count,COALESCE(BOOL_OR(m.user_id=?),false) AS is_member FROM team_challenges t LEFT JOIN team_challenge_members m ON m.team_challenge_id=t.id GROUP BY t.id ORDER BY t.week_start DESC,t.id DESC",uid(a));
 }
 @GetMapping("/teams/{id}/leaderboard") public List<Map<String,Object>> teamLeaderboard(@PathVariable long id,Authentication a){
  long user=uid(a);Integer member=jdbc.queryForObject("SELECT COUNT(*) FROM team_challenge_members WHERE team_challenge_id=? AND user_id=?",Integer.class,id,user);
  if(member==null||member==0)throw new IllegalArgumentException("Team not found or you are not a member");
  return jdbc.queryForList("SELECT u.id AS user_id,u.name,COALESCE(SUM(e.xp_awarded),0) AS earned_xp FROM team_challenge_members m JOIN users u ON u.id=m.user_id JOIN team_challenges t ON t.id=m.team_challenge_id LEFT JOIN quest_completion_events e ON e.user_id=u.id AND e.completed_at>=t.week_start AND e.completed_at<t.week_start+INTERVAL '7 days' WHERE m.team_challenge_id=? GROUP BY u.id,u.name ORDER BY earned_xp DESC,u.id",id);
 }
}
