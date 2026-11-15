package com.lifequest.service;
import org.springframework.data.redis.core.StringRedisTemplate;import org.springframework.jdbc.core.JdbcTemplate;import org.springframework.stereotype.Service;import java.time.Duration;import java.util.*;
@Service public class LeaderboardService {
 private final JdbcTemplate jdbc;private final StringRedisTemplate redis;
 public LeaderboardService(JdbcTemplate j,StringRedisTemplate r){jdbc=j;redis=r;}
 public List<Map<String,Object>> weekly(){
  String key="lifequest:leaderboard:weekly:v1";
  try{List<String> cached=redis.opsForList().range(key,0,49);if(cached!=null&&!cached.isEmpty())return cached.stream().map(s->{String[] p=s.split("\\|",3);return Map.<String,Object>of("userId",Long.valueOf(p[0]),"name",p[1],"weeklyXp",Long.valueOf(p[2]));}).toList();}catch(Exception ignored){}
  var rows=jdbc.queryForList("SELECT u.id AS user_id,u.name,COALESCE(SUM(e.xp_awarded),0) AS weekly_xp FROM users u LEFT JOIN quest_completion_events e ON e.user_id=u.id AND e.completed_at >= date_trunc('week', now()) AND e.completed_at < date_trunc('week', now()) + interval '7 days' GROUP BY u.id,u.name ORDER BY weekly_xp DESC,u.id ASC LIMIT 50");
  try{for(var row:rows)redis.opsForList().rightPush(key,row.get("user_id")+"|"+row.get("name")+"|"+row.get("weekly_xp"));redis.expire(key,Duration.ofMinutes(2));}catch(Exception ignored){}
  return rows;
 }
}
