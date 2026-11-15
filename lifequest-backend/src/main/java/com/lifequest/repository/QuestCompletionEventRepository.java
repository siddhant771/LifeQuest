package com.lifequest.repository;
import com.lifequest.entity.QuestCompletionEvent;import org.springframework.data.jpa.repository.JpaRepository;
public interface QuestCompletionEventRepository extends JpaRepository<QuestCompletionEvent,Long>{boolean existsByQuest_Id(Long questId);long countByUser_Id(Long userId);}
