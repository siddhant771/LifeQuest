package com.lifequest.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "quest_completion_events",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_event_quest",
                        columnNames = "quest_id"
                )
        }
)
public class QuestCompletionEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quest_id", nullable = false)
    private Quest quest;

    @Column(name = "xp_awarded", nullable = false)
    private Integer xpAwarded;

    @Column(name = "completed_at", nullable = false)
    private OffsetDateTime completedAt;

    public QuestCompletionEvent() {
    }

    public QuestCompletionEvent(
            User user,
            Quest quest,
            Integer xpAwarded,
            OffsetDateTime completedAt
    ) {
        this.user = user;
        this.quest = quest;
        this.xpAwarded = xpAwarded;
        this.completedAt = completedAt;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Quest getQuest() {
        return quest;
    }

    public Integer getXpAwarded() {
        return xpAwarded;
    }

    public OffsetDateTime getCompletedAt() {
        return completedAt;
    }
}