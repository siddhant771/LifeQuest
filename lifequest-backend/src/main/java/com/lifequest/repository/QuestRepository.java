package com.lifequest.repository;

import com.lifequest.entity.Quest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface QuestRepository extends JpaRepository<Quest, Long> {
    List<Quest> findByUser_Id(Long userId);

    Optional<Quest> findByIdAndUser_Id(Long questId, Long userId);
}