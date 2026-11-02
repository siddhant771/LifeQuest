package com.lifequest.controller;

import com.lifequest.entity.Quest;
import com.lifequest.service.QuestService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quests")
public class QuestController {

    private final QuestService questService;

    public QuestController(QuestService questService) {
        this.questService = questService;
    }

    @PostMapping
    public Quest createQuest(@RequestBody Quest quest) {
        return questService.createQuest(quest);
    }

    @GetMapping
    public List<Quest> getAllQuests() {
        return questService.getAllQuests();
    }
}