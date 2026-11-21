package com.lifequest.repository;
import com.lifequest.entity.Achievement;import org.springframework.data.jpa.repository.JpaRepository;import java.util.List;
public interface AchievementRepository extends JpaRepository<Achievement,Long>{boolean existsByUser_IdAndCode(Long uid,String code);List<Achievement> findByUser_IdOrderByUnlockedAtDesc(Long uid);}
