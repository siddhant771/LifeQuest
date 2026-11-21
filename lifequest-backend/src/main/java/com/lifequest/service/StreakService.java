package com.lifequest.service;
import com.lifequest.entity.User;import org.springframework.stereotype.Service;import java.time.LocalDate;
@Service public class StreakService {
 public void onValidatedQuestCompletion(User u,LocalDate utcDate){u.recordActivity(utcDate);}
 public int currentStreak(User u,LocalDate today){if(u.getLastActivityDate()==null)return 0;return u.getLastActivityDate().equals(today)||u.getLastActivityDate().equals(today.minusDays(1))?u.getCurrentStreak():0;}
}
