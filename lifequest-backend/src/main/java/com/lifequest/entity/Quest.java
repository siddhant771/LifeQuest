package com.lifequest.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "quests")
public class Quest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false)
    private Integer xp;

    @Column(nullable = false, length = 20)
    private String difficulty;

    @Column(nullable = false)
    private boolean completed = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

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

    public User getUser() {
        return user;
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

    public void setUser(User user) {
        this.user = user;
    }
}