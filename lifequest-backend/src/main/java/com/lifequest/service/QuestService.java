package com.lifequest.service;
import com.lifequest.dto.CreateQuestRequest;import com.lifequest.entity.*;import com.lifequest.repository.*;import org.springframework.stereotype.Service;import org.springframework.transaction.annotation.Transactional;import java.time.*;import java.util.*;
@Service public class QuestService {
 private final QuestRepository quests;private final UserRepository users;private final QuestCompletionEventRepository events;private final AchievementService achievements;private final StreakService streaks;
 public QuestService(QuestRepository q,UserRepository u,QuestCompletionEventRepository e,AchievementService a,StreakService s){quests=q;users=u;events=e;achievements=a;streaks=s;}
 @Transactional public Quest createQuest(Long uid,CreateQuestRequest r){
  if(r.clientRequestId()!=null&&!r.clientRequestId().isBlank()){var old=quests.findByUser_IdAndClientRequestId(uid,r.clientRequestId());if(old.isPresent())return old.get();}
  User u=users.findById(uid).orElseThrow(()->new IllegalArgumentException("User not found"));String d=r.difficulty().trim().toUpperCase(Locale.ROOT);
  int multiplier=switch(d){case "EASY"->1;case "MEDIUM"->2;case "HARD"->3;default->throw new IllegalArgumentException("Invalid difficulty");};
  if(r.xp()*multiplier>1000)throw new IllegalArgumentException("Maximum XP for "+d+" quests is "+(1000/multiplier));
  Quest q=new Quest();q.setTitle(r.title().trim());q.setXp(r.xp());q.setDifficulty(d);q.setCompleted(false);q.setUser(u);q.setClientRequestId(r.clientRequestId());return quests.save(q);
 }
 @Transactional(readOnly=true) public List<Quest> getUserQuests(Long uid){return quests.findByUser_IdOrderByIdDesc(uid);}
 @Transactional public QuestCompletionEvent completeQuest(Long uid,Long qid){User u=users.findById(uid).orElseThrow(()->new IllegalArgumentException("User not found"));Quest q=quests.findOwnedForUpdate(qid,uid).orElseThrow(()->new IllegalArgumentException("Quest not found"));if(q.isCompleted()||events.existsByQuest_Id(qid))throw new IllegalStateException("Quest already completed");int multiplier=switch(q.getDifficulty()){case "EASY"->1;case "MEDIUM"->2;case "HARD"->3;default->throw new IllegalArgumentException("Invalid stored difficulty");};int awarded=Math.multiplyExact(q.getXp(),multiplier);if(awarded<1||awarded>1000)throw new IllegalArgumentException("Calculated XP reward outside allowed range");q.setCompleted(true);u.addXp(awarded);streaks.onValidatedQuestCompletion(u,LocalDate.now(ZoneOffset.UTC));QuestCompletionEvent event=events.save(new QuestCompletionEvent(u,q,awarded,OffsetDateTime.now(ZoneOffset.UTC)));achievements.evaluate(u);return event;}
}
