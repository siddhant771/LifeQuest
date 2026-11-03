package com.lifequest.service;

import com.lifequest.entity.Quest;
import com.lifequest.entity.QuestCompletionEvent;
import com.lifequest.entity.User;

import com.lifequest.repository.QuestRepository;
import com.lifequest.repository.QuestCompletionEventRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class QuestCompletionService {

    private final QuestRepository questRepository;
    private final QuestCompletionEventRepository eventRepository;

    public QuestCompletionService(
            QuestRepository questRepository,
            QuestCompletionEventRepository eventRepository
    ) {
        this.questRepository = questRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional
    public QuestCompletionEvent completeQuest(
            Long userId,
            Long questId
    ) {

        Quest quest = questRepository
                .findByIdAndUser_Id(questId, userId)
                .orElseThrow(() ->
                        new RuntimeException("Quest not found"));

        if (quest.isCompleted()) {
            throw new RuntimeException("Quest already completed");
        }

        quest.setCompleted(true);

        QuestCompletionEvent event = new QuestCompletionEvent(
                quest.getUser(),
                quest,
                quest.getXp(),
                OffsetDateTime.now()
        );

        questRepository.save(quest);

        return eventRepository.save(event);
    }
}