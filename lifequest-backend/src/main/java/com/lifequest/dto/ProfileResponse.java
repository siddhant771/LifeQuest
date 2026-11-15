package com.lifequest.dto;
public record ProfileResponse(Long userId,String name,String email,long totalXp,int currentStreak,int longestStreak) {}
