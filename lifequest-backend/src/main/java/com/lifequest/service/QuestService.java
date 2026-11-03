package com.lifequest.service;

import com.lifequest.dto.CreateQuestRequest;
import com.lifequest.entity.Quest;
import com.lifequest.entity.User;
import com.lifequest.repository.QuestRepository;
import com.lifequest.repository.UserRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestService {

    private final QuestRepository questRepository;
    private final UserRepository userRepository;

    public QuestService(
            QuestRepository questRepository,
            UserRepository userRepository
    ) {
        this.questRepository = questRepository;
        this.userRepository = userRepository;
    }

    public Quest createQuest(Long userId, CreateQuestRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Quest quest = new Quest();

        quest.setTitle(request.getTitle());
        quest.setXp(request.getXp());
        quest.setDifficulty(request.getDifficulty().toUpperCase());
        quest.setCompleted(false);
        quest.setUser(user);

        return questRepository.save(quest);
    }

    public List<Quest> getUserQuests(Long userId) {
        return questRepository.findByUser_Id(userId);
    }
}