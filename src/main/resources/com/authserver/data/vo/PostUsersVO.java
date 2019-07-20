package com.authserver.data.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostUsersVO {

    private String email;
    private String password;
    private String nickname;
    private String fullName;
    private String gender;
    private Long dateOfBirth;
}