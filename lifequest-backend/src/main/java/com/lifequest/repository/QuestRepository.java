package com.lifequest.repository;
import com.lifequest.entity.Quest;import jakarta.persistence.LockModeType;import org.springframework.data.jpa.repository.*;import org.springframework.data.repository.query.Param;import java.util.*;
public interface QuestRepository extends JpaRepository<Quest,Long>{
 List<Quest> findByUser_IdOrderByIdDesc(Long userId);
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select q from Quest q where q.id=:qid and q.user.id=:uid") Optional<Quest> findOwnedForUpdate(@Param("qid") Long questId,@Param("uid") Long userId);
}
