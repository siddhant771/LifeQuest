package com.lifequest.dto;
import java.time.OffsetDateTime;
public record AchievementResponse(Long id,String code,OffsetDateTime unlockedAt) {}
