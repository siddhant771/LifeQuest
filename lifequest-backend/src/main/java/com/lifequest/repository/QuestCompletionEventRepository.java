package com.lifequest.repository;
import com.lifequest.entity.QuestCompletionEvent;import org.springframework.data.jpa.repository.JpaRepository;import java.util.Optional;
public interface QuestCompletionEventRepository extends JpaRepository<QuestCompletionEvent,Long>{boolean existsByQuest_Id(Long questId);long countByUser_Id(Long userId);Optional<QuestCompletionEvent> findByQuest_Id(Long questId);}
