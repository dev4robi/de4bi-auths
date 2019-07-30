package com.authserver.data.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ValidateUserJwtVO {

    private String audience;
    private String userJwt;
}