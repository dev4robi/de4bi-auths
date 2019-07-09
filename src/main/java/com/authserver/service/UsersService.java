package com.authserver.service;

import com.authserver.data.ApiResult;
import com.authserver.data.jpa.enums.UsersStatus;
import com.authserver.data.jpa.repository.UsersRepository;
import com.authserver.data.jpa.table.Users;
import com.authserver.util.ValidatorUtil;
import com.robi.util.CipherUtil;
import com.robi.util.MapUtil;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@PropertySource("config.properties")
@Service
public class UsersService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private UsersRepository usersRepo;
    private Environment env;

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

        // Password salting and hashing
        ApiResult hashingPasswordRst = hashingPassword(password);

        if (!hashingPasswordRst.getResult()) {
            logger.error("'hashingPasswordRst' is false!");
            return hashingPasswordRst;
        }

        password = hashingPasswordRst.getData().get("password").toString();

        // JPA - insert into users
        try {
            Long curTime = System.currentTimeMillis();
            Users insertedUser = Users.builder().email(email)
                                                .password(password)
                                                .nickname(nickname)
                                                .fullName(fullName)
                                                .gender(gender.charAt(0))
                                                .dateOfBirth(dateOfBirth)
                                                .accessLevel(0)
                                                .status(UsersStatus.NORMAL)
                                                .joinTime(curTime)
                                                .lastLoginTime(0L)
                                                .accessibleTime(curTime)
                                                .build();

            if (usersRepo.save(insertedUser) == null) {
                logger.error("JPA - Fail to insert 'insertedUser'!");
                throw new Exception();
            }

            logger.info("JPA - insert success! (insertedUser:" + insertedUser.toString() + ")");
        }
        catch (Exception e) {
            logger.error("Exception!", e);
            return ApiResult.make(false, "회원정보 DB추가중 오류가 발생했습니다.");
        }

        return ApiResult.make(true);
    }

    // 비밀번호에 솔트해싱 수행
    private ApiResult hashingPassword(String password) {
        try {
            byte[] hashingPassword = CipherUtil.hashing(CipherUtil.SHA256, password.getBytes(), env.getProperty("users.password.salt").getBytes());

            if (hashingPassword == null) {
                logger.error("'hashingPassword' is null!");
                throw new Exception();
            }

            password = Hex.encodeHexString(hashingPassword);
        }
        catch (Exception e) {
            logger.error("Exception while password hashing!", e);
            return ApiResult.make(false, "비밀번호 암호화중 오류가 발생했습니다.");
        }

        return ApiResult.make(true, null, MapUtil.toMap("password", password));
    }
}