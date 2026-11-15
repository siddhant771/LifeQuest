package com.lifequest.service;
import com.lifequest.entity.*;import com.lifequest.repository.*;import org.junit.jupiter.api.*;import org.junit.jupiter.api.extension.ExtendWith;import org.mockito.*;import org.mockito.junit.jupiter.MockitoExtension;import java.util.Optional;import static org.junit.jupiter.api.Assertions.*;import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class) class QuestServiceTest {
 @Mock QuestRepository quests;@Mock UserRepository users;@Mock QuestCompletionEventRepository events;@Mock AchievementService achievements;@Mock StreakService streaks;@InjectMocks QuestService service;
 @Test void completionAwardsServerCalculatedDifficultyXp(){
  User u=new User();Quest q=new Quest();q.setTitle("Read");q.setXp(50);q.setDifficulty("HARD");q.setUser(u);
  when(users.findById(7L)).thenReturn(Optional.of(u));when(quests.findOwnedForUpdate(11L,7L)).thenReturn(Optional.of(q));when(events.existsByQuest_Id(11L)).thenReturn(false);when(events.save(any())).thenAnswer(inv->inv.getArgument(0));
  QuestCompletionEvent event=service.completeQuest(7L,11L);
  assertEquals(150,event.getXpAwarded());assertEquals(150,u.getTotalXp());assertTrue(q.isCompleted());verify(achievements).evaluate(u);
 }
 @Test void duplicateCompletionIsRejectedWithoutMoreXp(){
  User u=new User();Quest q=new Quest();q.setXp(50);q.setDifficulty("EASY");q.setCompleted(true);
  when(users.findById(7L)).thenReturn(Optional.of(u));when(quests.findOwnedForUpdate(11L,7L)).thenReturn(Optional.of(q));
  assertThrows(IllegalStateException.class,()->service.completeQuest(7L,11L));assertEquals(0,u.getTotalXp());verify(events,never()).save(any());
 }
}
