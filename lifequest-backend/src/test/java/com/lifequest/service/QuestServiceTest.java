package com.lifequest.service;

import com.lifequest.dto.CreateQuestRequest;
import com.lifequest.entity.Quest;
import com.lifequest.entity.QuestCompletionEvent;
import com.lifequest.entity.User;
import com.lifequest.repository.QuestCompletionEventRepository;
import com.lifequest.repository.QuestRepository;
import com.lifequest.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestServiceTest {
    @Mock QuestRepository quests;
    @Mock UserRepository users;
    @Mock QuestCompletionEventRepository events;
    @Mock AchievementService achievements;
    @Mock StreakService streaks;

    @InjectMocks QuestService service;

    @Test
    void completionAwardsServerCalculatedDifficultyXp() {
        User user = new User();
        Quest quest = quest(user, 50, "HARD", false);

        when(users.findById(7L)).thenReturn(Optional.of(user));
        when(quests.findOwnedForUpdate(11L, 7L)).thenReturn(Optional.of(quest));
        when(events.existsByQuest_Id(11L)).thenReturn(false);
        when(events.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        QuestCompletionEvent event = service.completeQuest(7L, 11L);

        assertEquals(150, event.getXpAwarded());
        assertEquals(150, user.getTotalXp());
        assertTrue(quest.isCompleted());
        verify(streaks).onValidatedQuestCompletion(eq(user), any());
        verify(achievements).evaluate(user);
        verify(events).save(any(QuestCompletionEvent.class));
    }

    @Test
    void duplicateCompletionIsRejectedWithoutMoreXp() {
        User user = new User();
        Quest quest = quest(user, 50, "EASY", true);
        when(users.findById(7L)).thenReturn(Optional.of(user));
        when(quests.findOwnedForUpdate(11L, 7L)).thenReturn(Optional.of(quest));

        assertThrows(IllegalStateException.class, () -> service.completeQuest(7L, 11L));

        assertEquals(0, user.getTotalXp());
        verify(events, never()).save(any());
        verify(streaks, never()).onValidatedQuestCompletion(any(), any());
        verify(achievements, never()).evaluate(any());
    }

    @Test
    void duplicateCompletionEventIsAlsoRejected() {
        User user = new User();
        Quest quest = quest(user, 20, "MEDIUM", false);
        when(users.findById(7L)).thenReturn(Optional.of(user));
        when(quests.findOwnedForUpdate(11L, 7L)).thenReturn(Optional.of(quest));
        when(events.existsByQuest_Id(11L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> service.completeQuest(7L, 11L));

        assertEquals(0, user.getTotalXp());
        assertFalse(quest.isCompleted());
        verify(events, never()).save(any());
    }

    @Test
    void completionRejectsUnknownUser() {
        when(users.findById(404L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.completeQuest(404L, 11L));

        verifyNoInteractions(quests, events, achievements, streaks);
    }

    @Test
    void completionRejectsQuestNotOwnedOrNotFound() {
        when(users.findById(7L)).thenReturn(Optional.of(new User()));
        when(quests.findOwnedForUpdate(11L, 7L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.completeQuest(7L, 11L));

        verify(events, never()).save(any());
        verifyNoInteractions(achievements, streaks);
    }

    @Test
    void completionRejectsInvalidStoredDifficulty() {
        User user = new User();
        Quest quest = quest(user, 20, "EXTREME", false);
        when(users.findById(7L)).thenReturn(Optional.of(user));
        when(quests.findOwnedForUpdate(11L, 7L)).thenReturn(Optional.of(quest));
        when(events.existsByQuest_Id(11L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> service.completeQuest(7L, 11L));

        assertEquals(0, user.getTotalXp());
        verify(events, never()).save(any());
    }

    @Test
    void completionRejectsCalculatedRewardOutsideAllowedRange() {
        User user = new User();
        Quest quest = quest(user, 1000, "HARD", false);
        when(users.findById(7L)).thenReturn(Optional.of(user));
        when(quests.findOwnedForUpdate(11L, 7L)).thenReturn(Optional.of(quest));
        when(events.existsByQuest_Id(11L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> service.completeQuest(7L, 11L));

        assertEquals(0, user.getTotalXp());
        verify(events, never()).save(any());
    }

    @Test
    void createQuestNormalizesDifficultyAndTitle() {
        User user = new User();
        CreateQuestRequest request = new CreateQuestRequest("  Read a book  ", 40, " medium ", "request-1");
        when(users.findById(7L)).thenReturn(Optional.of(user));
        when(quests.save(any(Quest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Quest created = service.createQuest(7L, request);

        assertEquals("Read a book", created.getTitle());
        assertEquals("MEDIUM", created.getDifficulty());
        assertEquals(40, created.getXp());
        assertEquals("request-1", created.getClientRequestId());
        assertSame(user, created.getUser());
        verify(quests).save(created);
    }

    @Test
    void createQuestReturnsExistingQuestForRepeatedClientRequestId() {
        User user = new User();
        Quest existing = quest(user, 25, "EASY", false);
        when(quests.findByUser_IdAndClientRequestId(7L, "same-request")).thenReturn(Optional.of(existing));

        Quest result = service.createQuest(
                7L, new CreateQuestRequest("Different title", 100, "HARD", "same-request"));

        assertSame(existing, result);
        verifyNoInteractions(users);
        verify(quests, never()).save(any());
    }

    @Test
    void createQuestRejectsUnknownUser() {
        when(users.findById(404L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.createQuest(
                404L, new CreateQuestRequest("Read", 10, "EASY", null)));

        verify(quests, never()).save(any());
    }

    @Test
    void createQuestRejectsUnsupportedDifficulty() {
        when(users.findById(7L)).thenReturn(Optional.of(new User()));

        assertThrows(IllegalArgumentException.class, () -> service.createQuest(
                7L, new CreateQuestRequest("Read", 10, "IMPOSSIBLE", null)));

        verify(quests, never()).save(any());
    }

    @Test
    void createQuestEnforcesDifficultyAdjustedXpLimit() {
        when(users.findById(7L)).thenReturn(Optional.of(new User()));

        assertThrows(IllegalArgumentException.class, () -> service.createQuest(
                7L, new CreateQuestRequest("Read", 334, "HARD", null)));

        verify(quests, never()).save(any());
    }

    private Quest quest(User user, int xp, String difficulty, boolean completed) {
        Quest quest = new Quest();
        quest.setTitle("Read");
        quest.setXp(xp);
        quest.setDifficulty(difficulty);
        quest.setCompleted(completed);
        quest.setUser(user);
        return quest;
    }
}
