package com.lifequest.entity;
import jakarta.persistence.*;import java.time.OffsetDateTime;
@Entity @Table(name="user_achievements",uniqueConstraints=@UniqueConstraint(columnNames={"user_id","achievement_code"}))
public class Achievement {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="user_id",nullable=false) private User user;
 @Column(name="achievement_code",nullable=false,length=40) private String code;
 @Column(name="unlocked_at",nullable=false) private OffsetDateTime unlockedAt;
 protected Achievement(){} public Achievement(User u,String c,OffsetDateTime t){user=u;code=c;unlockedAt=t;}
 public Long getId(){return id;}public String getCode(){return code;}public OffsetDateTime getUnlockedAt(){return unlockedAt;}
}
