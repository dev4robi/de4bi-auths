package com.authserver.data.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IssueUserJwtVO {

    private String audiance;
    private String email;
    private String password;
    private Long duration;
}