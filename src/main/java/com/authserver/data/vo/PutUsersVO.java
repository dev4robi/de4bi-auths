package com.authserver.data.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PutUsersVO {

    private String password;
    private String newPassword;
    private String fullName;
    private String gender;
    private Long dateOfBirth;
}