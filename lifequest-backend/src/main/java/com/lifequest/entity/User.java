package com.lifequest.entity;
import jakarta.persistence.*;
import java.time.LocalDate;
@Entity @Table(name="users")
public class User {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,length=100) private String name;
 @Column(nullable=false,unique=true,length=150) private String email;
 @Column(name="password_hash",nullable=false) private String passwordHash;
 @Column(name="total_xp",nullable=false) private long totalXp=0;
 @Column(name="current_streak",nullable=false) private int currentStreak=0;
 @Column(name="longest_streak",nullable=false) private int longestStreak=0;
 @Column(name="last_activity_date") private LocalDate lastActivityDate;
 public User(){}
 public Long getId(){return id;} public String getName(){return name;} public String getEmail(){return email;}
 public String getPasswordHash(){return passwordHash;} public long getTotalXp(){return totalXp;}
 public int getCurrentStreak(){return currentStreak;} public int getLongestStreak(){return longestStreak;} public LocalDate getLastActivityDate(){return lastActivityDate;}
 public void setName(String v){name=v;} public void setEmail(String v){email=v;} public void setPasswordHash(String v){passwordHash=v;}
 public void addXp(long v){totalXp=Math.addExact(totalXp,v);}
 public void recordActivity(LocalDate today){if(today.equals(lastActivityDate))return;currentStreak=today.minusDays(1).equals(lastActivityDate)?currentStreak+1:1;longestStreak=Math.max(longestStreak,currentStreak);lastActivityDate=today;}
}
