package com.lifequest.service;
import com.lifequest.entity.User;import org.junit.jupiter.api.Test;import java.time.LocalDate;import static org.junit.jupiter.api.Assertions.*;
class StreakServiceTest {
 private final StreakService service=new StreakService();
 @Test void sameDayActivityDoesNotIncrementTwice(){User u=new User();LocalDate d=LocalDate.of(2026,11,15);u.recordActivity(d);u.recordActivity(d);assertEquals(1,u.getCurrentStreak());}
 @Test void consecutiveDayIncrementsStreak(){User u=new User();u.recordActivity(LocalDate.of(2026,11,14));u.recordActivity(LocalDate.of(2026,11,15));assertEquals(2,u.getCurrentStreak());assertEquals(2,u.getLongestStreak());}
 @Test void missedDayResetsCurrentStreak(){User u=new User();u.recordActivity(LocalDate.of(2026,11,12));u.recordActivity(LocalDate.of(2026,11,15));assertEquals(1,u.getCurrentStreak());assertEquals(1,u.getLongestStreak());}
 @Test void recoveryWindowKeepsYesterdayStreakUntilEndOfToday(){User u=new User();u.recordActivity(LocalDate.of(2026,11,14));assertEquals(1,service.currentStreak(u,LocalDate.of(2026,11,15)));assertEquals(0,service.currentStreak(u,LocalDate.of(2026,11,16)));}
}
