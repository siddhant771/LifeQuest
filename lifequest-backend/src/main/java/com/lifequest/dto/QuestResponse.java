package com.lifequest.dto;



public record QuestResponse(
        Long id,
        Long userId,
        String title,
        Integer xp,
        String difficulty,
        boolean completed
) {
}