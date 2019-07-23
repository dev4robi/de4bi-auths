package com.authserver.service;

import javax.transaction.Transactional;

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

    public ApiResult selectUserByKey(String keyName, String value) {
        // Param check
        ApiResult paramValidationRst = null;
        
        if (keyName == null) {
            logger.error("'keyName' is null!");
            return ApiResult.make(false);
        }

        // JPA - select from users
        Users selectedUser = null;

        try {
            switch (keyName) {
                case "id":
                    if (!(paramValidationRst = ValidatorUtil.arthimatic(keyName, value, 1L, Long.MAX_VALUE)).getResult()) {
                        return paramValidationRst;
                    }
                    selectedUser = usersRepo.findById(Long.valueOf(value)).get();
                    break;
                case "email":
                    if (!(paramValidationRst = ValidatorUtil.isEmail(value)).getResult()) {
                        return paramValidationRst;
                    }
                    selectedUser = usersRepo.findByEmail(value);
                    break;
                case "nickname":
                    if (!(paramValidationRst = ValidatorUtil.nullOrZeroLen(keyName, value)).getResult()) {
                        return paramValidationRst;
                    }
                    selectedUser = usersRepo.findByNickname(value);
                    break;
                default:
                    logger.error("Undefined 'keyName'! (keyName:" + keyName + ")");
                    return ApiResult.make(false);
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

    @Transactional
    public ApiResult updateUser(String userJwt, String password, String nickname,
                                String fullName, String gender, Long dateOfBirth) {
        final String email = "robi9202@gmail.com"; // @@ jwt분석구문 추후 추가!!

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
        if (!(paramValidationRst = selectUserByKey(null, email)).getResult()) {
            logger.error("selectUserByEmail() return false!");
            return paramValidationRst;
        }

        Users updatedUser = (Users) paramValidationRst.getData("selectedUser");

        if (updatedUser == null) {
            logger.error("'updatedUser' is null!");
            return ApiResult.make(false);
        }

        // JPA - update users
        try {
            // [Note] 빌더로 새 Users 객체를 생성하여 save()하면, Duplicated Key 오류 발생.
            updatedUser.setPassword(password);
            updatedUser.setNickname(nickname);
            updatedUser.setFullName(fullName);
            updatedUser.setGender(gender.charAt(0));
            updatedUser.setDateOfBirth(dateOfBirth);
            
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

    @Transactional
    public ApiResult deleteUser(String userJwt) {
        final String email = "robi02@naver.com"; // @@ jwt분석구문 추후 추가!!

        // Param check
        ApiResult paramValidationRst = null;

        if (!(paramValidationRst = ValidatorUtil.isEmail(email)).getResult()) {
            return paramValidationRst;
        }

        if (!(paramValidationRst = selectUserByKey(null, email)).getResult()) {
            logger.error("selectUserByEmail() return false!");
            return paramValidationRst;
        }

        Users deletedUser = (Users) paramValidationRst.getData("selectedUser");

        if (deletedUser.getStatus() == UsersStatus.DEREGISTERED) {
            logger.error("User already deregestered! (email: " + email + ")");
            return ApiResult.make(false, "이미 탈퇴한 회원입니다.");
        }

        // JPA - delete(update) users
        try { 
            deletedUser.setStatus(UsersStatus.DEREGISTERED);
            deletedUser.setLastLoginTime(System.currentTimeMillis());

            if (usersRepo.save(deletedUser) == null) {
                logger.error("save() for update return null!");
                throw new Exception();
            }

            logger.info("JPA - delete(update) success! (deletedUser:" + deletedUser.toString() + ")");
        }
        catch (Exception e) {
            logger.error("JPA Exception!", e);
            return ApiResult.make(false, "회원 탈퇴중 DB오류가 발생했습니다.");
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