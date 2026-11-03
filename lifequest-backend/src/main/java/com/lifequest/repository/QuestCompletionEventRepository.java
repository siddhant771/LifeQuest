package com.lifequest.repository;

import com.lifequest.entity.QuestCompletionEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestCompletionEventRepository
        extends JpaRepository<QuestCompletionEvent, Long> {
}