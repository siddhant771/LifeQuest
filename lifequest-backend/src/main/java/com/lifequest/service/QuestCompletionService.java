package com.lifequest.service;
import com.lifequest.entity.QuestCompletionEvent;
import org.springframework.stereotype.Service;
@Service public class QuestCompletionService{
 private final QuestService questService;
 public QuestCompletionService(QuestService questService){this.questService=questService;}
 public QuestCompletionEvent completeQuest(Long userId,Long questId){return questService.completeQuest(userId,questId);}
}
