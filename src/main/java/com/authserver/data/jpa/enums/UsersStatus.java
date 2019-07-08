package com.authserver.data.jpa.enums;

import lombok.Getter;

@Getter
public enum UsersStatus  {
    
    NORMAL      (0, "일반"),
    SLEEPING    (1, "휴면"),
    BLACKLIST   (2, "정지"),
    DEREGISTERED(3, "탈퇴");

    private int code;
    private String value;

    private UsersStatus(int code, String value) {
        this.code = code;
        this.value = value;
    }
}