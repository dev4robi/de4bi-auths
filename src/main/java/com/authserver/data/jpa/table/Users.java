package com.authserver.data.jpa.table;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import com.authserver.data.jpa.converter.UsersStatusConverter;
import com.authserver.data.jpa.enums.UsersStatus;

import org.hibernate.annotations.DynamicUpdate;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "users", indexes = { @Index(columnList = "email"), @Index(columnList = "nickname") })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@ToString
public class Users {
    
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "email", length = 128, nullable = false, unique = true)
    private String email;

    @Column(name = "password", length = 32, nullable = false)
    private String password;

    @Column(name = "nickname", length = 16, nullable = false, unique = true)
    private String nickname;

    @Column(name = "full_name", length = 64, nullable = false)
    private String fullName;

    @Column(name = "gender", length = 1, nullable = false)
    private Character gender;

    @Column(name = "date_of_birth", nullable = false)
    private Long dateOfBirth;

    @Column(name = "access_level", nullable = false)
    private Integer accessLevel = 1;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    @Convert(converter = UsersStatusConverter.class)
    private UsersStatus status;

    @Column(name = "join_time", nullable = false)
    private Long joinTime;

    @Column(name = "last_login_time")
    private Long lastLoginTime;

    @Column(name = "accessible_time")
    private Long accessibleTime;

    @Builder(toBuilder = true)
    public Users(String email, String password, String nickname, String fullName,
                 Character gender, Long dateOfBirth, Integer accessLevel, UsersStatus status,
                 Long joinTime, Long lastLoginTime, Long accessibleTime) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.fullName = fullName;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.accessLevel = accessLevel;
        this.status = status;
        this.joinTime = joinTime;
        this.lastLoginTime = lastLoginTime;
        this.accessibleTime = accessibleTime;
    }
}