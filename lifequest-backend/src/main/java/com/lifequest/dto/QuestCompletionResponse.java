package com.lifequest.dto;

import java.time.OffsetDateTime;

public record QuestCompletionResponse(
        Long eventId,
        Long userId,
        Long questId,
        Integer xpAwarded,
        OffsetDateTime completedAt
) {
}