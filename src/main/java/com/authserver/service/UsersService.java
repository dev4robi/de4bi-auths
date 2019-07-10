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

    public ApiResult selectUserByEmail(String email) {
        // Param check
        ApiResult paramValidationRst = null;

        if (!(paramValidationRst = ValidatorUtil.isEmail(email)).getResult()) {
            return paramValidationRst;
        }

        // JPA - select from users
        Users selectedUser = null;

        try {
            selectedUser = usersRepo.findByEmail(email);

            if (selectedUser == null) {
                logger.error("'selectedUser' is null!");
                throw new Exception();
            }
        }
        catch (Exception e) {
            logger.error("JPA Exception!", e);
            return ApiResult.make(false, "회원정보 DB조회중 오류가 발생했습니다.");
        }

        return ApiResult.make(true, null, MapUtil.toMap("selectedUser", selectedUser));
    }

    public ApiResult insertUser(String email, String password, String nickname,
                                String fullName, String gender, Long dateOfBirth) {
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

        if (!(paramValidationRst = ValidatorUtil.arthimatic("dateOfBirth", dateOfBirth, 0L, Long.MAX_VALUE)).getResult()) {
            return paramValidationRst;
        }

        // Password salting and hashing
        ApiResult hashingPasswordRst = hashingPassword(password);

        if (!hashingPasswordRst.getResult()) {
            logger.error("'hashingPasswordRst' is false!");
            return hashingPasswordRst;
        }

        password = (String) hashingPasswordRst.getData("password");

        // JPA - insert into users
        try {
            Long curTime = System.currentTimeMillis();
            Users insertedUser = Users.builder().email(email).password(password)
                                                .nickname(nickname).fullName(fullName)
                                                .gender(gender.charAt(0)).dateOfBirth(dateOfBirth)
                                                .accessLevel(0).status(UsersStatus.NORMAL)
                                                .joinTime(curTime).lastLoginTime(0L)
                                                .accessibleTime(curTime)
                                                .build();

            if (usersRepo.save(insertedUser) == null) {
                logger.error("JPA - Fail to insert 'insertedUser'!");
                throw new Exception();
            }

            logger.info("JPA - insert success! (insertedUser:" + insertedUser.toString() + ")");
        }
        catch (Exception e) {
            logger.error("JPA Exception!", e);
            return ApiResult.make(false, "회원정보 DB추가중 오류가 발생했습니다.");
        }

        return ApiResult.make(true);
    }

    public ApiResult updateUser(String userJwt, String password, String nickname,
                                String fullName, String gender, Long dateOfBirth) {
        String email = "robi9202@gmail.com"; // @@ jwt분석구문 추후 추가!!
        // @@ 업데이트 시 Duplicate entry 'robi9202@gmail.com' for key 'users_uk_idx_email'
        // @@ 오류 발생하는것부터 해결...! 인덱스 중복...

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

        if (!(paramValidationRst = ValidatorUtil.arthimatic("dateOfBirth", dateOfBirth, 0L, Long.MAX_VALUE)).getResult()) {
            return paramValidationRst;
        }

        // Password salting and hashing
        ApiResult hashingPasswordRst = hashingPassword(password);

        if (!hashingPasswordRst.getResult()) {
            logger.error("'hashingPasswordRst' is false!");
            return hashingPasswordRst;
        }

        password = (String) hashingPasswordRst.getData("password");

        // Find users from DB
        if (!(paramValidationRst = selectUserByEmail(email)).getResult()) {
            logger.error("selectUserByEmail() return false!");
            return paramValidationRst;
        }

        Users selectedUser = (Users) paramValidationRst.getData("selectedUser");

        if (selectedUser == null) {
            logger.error("'selectedUser' is null!");
            return ApiResult.make(false);
        }

        // JPA - update users
        try {
            Users updatedUser = (selectedUser.toBuilder())
                                             .password(password)
                                             .nickname(nickname)
                                             .fullName(fullName)
                                             .gender(gender.charAt(0))
                                             .dateOfBirth(dateOfBirth)
                                             .build();

            if (usersRepo.save(updatedUser) == null) {
                logger.error("save() for update return null!");
                throw new Exception();
            }

            logger.info("JPA - update success! (updatedUser:" + updatedUser.toString() + ")");
        }
        catch (Exception e) {
            logger.error("JPA Exception!", e);
            return ApiResult.make(false, "회원정보 DB수정중 오류가 발생했습니다.");
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