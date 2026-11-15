package com.lifequest.service;
import com.lifequest.dto.LeaderboardEntry;import org.springframework.data.redis.core.StringRedisTemplate;import org.springframework.jdbc.core.JdbcTemplate;import org.springframework.stereotype.Service;import java.time.Duration;import java.util.*;
@Service public class LeaderboardService {
 private final JdbcTemplate jdbc;private final StringRedisTemplate redis;public LeaderboardService(JdbcTemplate j,StringRedisTemplate r){jdbc=j;redis=r;}
 public List<LeaderboardEntry> weekly(){
  String key="lifequest:leaderboard:weekly:v2";
  try{List<String> cached=redis.opsForList().range(key,0,49);if(cached!=null&&!cached.isEmpty())return cached.stream().map(s->{String[] p=s.split("\\|",3);return new LeaderboardEntry(Long.parseLong(p[0]),new String(Base64.getDecoder().decode(p[1]),java.nio.charset.StandardCharsets.UTF_8),Long.parseLong(p[2]));}).toList();}catch(Exception ignored){}
  List<LeaderboardEntry> rows=jdbc.query("SELECT u.id,u.name,COALESCE(SUM(e.xp_awarded),0) AS weekly_xp FROM users u LEFT JOIN quest_completion_events e ON e.user_id=u.id AND e.completed_at >= date_trunc('week', now()) AND e.completed_at < date_trunc('week', now()) + interval '7 days' GROUP BY u.id,u.name ORDER BY weekly_xp DESC,u.id ASC LIMIT 50",(rs,n)->new LeaderboardEntry(rs.getLong("id"),rs.getString("name"),rs.getLong("weekly_xp")));
  try{for(var row:rows)redis.opsForList().rightPush(key,row.userId()+"|"+Base64.getEncoder().encodeToString(row.name().getBytes(java.nio.charset.StandardCharsets.UTF_8))+"|"+row.weeklyXp());redis.expire(key,Duration.ofMinutes(2));}catch(Exception ignored){}
  return rows;
 }
}
