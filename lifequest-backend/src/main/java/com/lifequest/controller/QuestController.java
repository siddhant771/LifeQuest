package com.lifequest.controller;

import com.lifequest.dto.CreateQuestRequest;
import com.lifequest.dto.QuestResponse;
import com.lifequest.dto.QuestCompletionResponse;

import com.lifequest.entity.Quest;
import com.lifequest.entity.QuestCompletionEvent;

import com.lifequest.service.QuestService;
import com.lifequest.service.QuestCompletionService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class QuestController {

    private final QuestService questService;
    private final QuestCompletionService completionService;

    public QuestController(
            QuestService questService,
            QuestCompletionService completionService
    ) {
        this.questService = questService;
        this.completionService = completionService;
    }

    @PostMapping("/users/{userId}/quests")
    public QuestResponse createQuest(
            @PathVariable Long userId,
            @RequestBody CreateQuestRequest request
    ) {
        Quest quest = questService.createQuest(userId, request);
        return toResponse(quest);
    }

    @GetMapping("/users/{userId}/quests")
    public List<QuestResponse> getUserQuests(
            @PathVariable Long userId
    ) {
        return questService.getUserQuests(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @PostMapping("/users/{userId}/quests/{questId}/complete")
    public QuestCompletionResponse completeQuest(
            @PathVariable Long userId,
            @PathVariable Long questId
    ) {
        QuestCompletionEvent event =
                completionService.completeQuest(userId, questId);

        return new QuestCompletionResponse(
                event.getId(),
                event.getUser().getId(),
                event.getQuest().getId(),
                event.getXpAwarded(),
                event.getCompletedAt()
        );
    }

    private QuestResponse toResponse(Quest quest) {
        return new QuestResponse(
                quest.getId(),
                quest.getUser().getId(),
                quest.getTitle(),
                quest.getXp(),
                quest.getDifficulty(),
                quest.isCompleted()
        );
    }
}