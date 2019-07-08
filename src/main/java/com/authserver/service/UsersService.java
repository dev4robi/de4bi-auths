package com.authserver.service;

import com.authserver.data.ApiResult;
import com.authserver.data.jpa.repository.UsersRepository;
import com.authserver.util.ValidatorUtil;
import com.robi.util.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class UsersService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private UsersRepository usersRepo;

    public ApiResult insertUser(String email, String password, String nickname,
                                String fullName, String gender, long dateOfBirth) {
        // Param check
        ApiResult paramValidationRst = null;

        if (!(paramValidationRst = ValidatorUtil.isEmail(email)).getResult()) {
            return paramValidationRst;
        }

        if (!(paramValidationRst = ValidatorUtil.strLen("password", password, 8, 32)).getResult()) {
            return paramValidationRst;
        }

        if (!(paramValidationRst = ValidatorUtil.strLen("nickname", nickname, 4, 16)).getResult()) {
            return paramValidationRst;
        }

        if (!(paramValidationRst = ValidatorUtil.strLen("fullName", fullName, 2, 64)).getResult()) {
            return paramValidationRst;
        }

        if (!(paramValidationRst = ValidatorUtil.isGender(gender)).getResult()) {
            return paramValidationRst;
        }

        // JPA - insert into `users`
        try {
            // 여기부터 시작. UsersRepo 에러부터 끄자...! @@
        }
        catch (Exception e) {
            logger.error("Exception!", e);
            return ApiResult.make(false, "회원DB추가중 오류가 발생했습니다.");
        }        

        return null;
    }
}