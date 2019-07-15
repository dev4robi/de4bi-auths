package com.authserver.data.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DeleteUsersVO {

    private String userJwt;
    private String password;
}