package com.lifequest.service;
import com.lifequest.entity.*;import com.lifequest.repository.*;import org.springframework.stereotype.Service;import java.time.OffsetDateTime;import java.util.*;
@Service public class AchievementService {
 private final AchievementRepository repo;private final QuestCompletionEventRepository events;
 public AchievementService(AchievementRepository r,QuestCompletionEventRepository e){repo=r;events=e;}
 public void evaluate(User u){long n=events.countByUser_Id(u.getId());if(n>=1)unlock(u,"FIRST_QUEST");if(n>=5)unlock(u,"FIVE_QUESTS");if(u.getLongestStreak()>=7)unlock(u,"WEEK_STREAK");}
 private void unlock(User u,String c){if(!repo.existsByUser_IdAndCode(u.getId(),c))repo.save(new Achievement(u,c,OffsetDateTime.now(ZoneOffset.UTC)));}
 public List<Achievement> list(Long uid){return repo.findByUser_IdOrderByUnlockedAtDesc(uid);}
}
