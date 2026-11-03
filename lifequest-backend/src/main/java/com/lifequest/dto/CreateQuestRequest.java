package com.lifequest.dto;

public class CreateQuestRequest {

    private String title;
    private Integer xp;
    private String difficulty;

    public String getTitle() {
        return title;
    }

    public Integer getXp() {
        return xp;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setXp(Integer xp) {
        this.xp = xp;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }
}