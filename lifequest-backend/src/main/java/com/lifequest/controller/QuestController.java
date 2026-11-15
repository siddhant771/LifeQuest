package com.lifequest.controller;
import com.lifequest.dto.*;import com.lifequest.entity.*;import com.lifequest.service.QuestService;import jakarta.validation.Valid;import org.springframework.security.core.Authentication;import org.springframework.web.bind.annotation.*;import java.util.*;
@RestController @RequestMapping("/api/quests") public class QuestController {
 private final QuestService service;public QuestController(QuestService s){service=s;}private Long uid(Authentication a){return Long.valueOf(a.getName());}
 @PostMapping public QuestResponse create(@Valid @RequestBody CreateQuestRequest r,Authentication a){return response(service.createQuest(uid(a),r));}
 @GetMapping public List<QuestResponse> list(Authentication a){return service.getUserQuests(uid(a)).stream().map(this::response).toList();}
 @PostMapping("/{questId}/complete") public QuestCompletionResponse complete(@PathVariable Long questId,Authentication a){QuestCompletionEvent e=service.completeQuest(uid(a),questId);return new QuestCompletionResponse(e.getId(),e.getUser().getId(),e.getQuest().getId(),e.getXpAwarded(),e.getCompletedAt());}
 private QuestResponse response(Quest q){return new QuestResponse(q.getId(),q.getUser().getId(),q.getTitle(),q.getXp(),q.getDifficulty(),q.isCompleted());}
}
