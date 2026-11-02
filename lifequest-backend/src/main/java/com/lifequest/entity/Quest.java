package com.lifequest.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "quests")
public class Quest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private Integer xp;

    private String difficulty;

    private boolean completed = false;

    public Quest() {
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Integer getXp() {
        return xp;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public boolean isCompleted() {
        return completed;
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

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}