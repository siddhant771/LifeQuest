package com.lifequest.entity;
import jakarta.persistence.*;
@Entity @Table(name="quests",uniqueConstraints=@UniqueConstraint(name="uq_quest_client_request",columnNames={"user_id","client_request_id"}))
public class Quest {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,length=200) private String title;
 @Column(nullable=false) private Integer xp;
 @Column(nullable=false,length=20) private String difficulty;
 @Column(nullable=false) private boolean completed=false;
 @Column(name="client_request_id",length=80) private String clientRequestId;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="user_id",nullable=false) private User user;
 public Quest(){} public Long getId(){return id;}public String getTitle(){return title;}public Integer getXp(){return xp;}public String getDifficulty(){return difficulty;}public boolean isCompleted(){return completed;}public User getUser(){return user;}public String getClientRequestId(){return clientRequestId;}
 public void setTitle(String v){title=v;}public void setXp(Integer v){xp=v;}public void setDifficulty(String v){difficulty=v;}public void setCompleted(boolean v){completed=v;}public void setUser(User v){user=v;}public void setClientRequestId(String v){clientRequestId=v;}
}
