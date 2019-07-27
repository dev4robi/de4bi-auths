package com.authserver.service;

import java.security.Key;
import java.security.KeyFactory;
import java.util.Date;

import javax.crypto.spec.SecretKeySpec;
import javax.transaction.Transactional;

import com.authserver.data.ApiResult;
import com.authserver.data.jpa.enums.UsersStatus;
import com.authserver.data.jpa.repository.UsersRepository;
import com.authserver.data.jpa.table.Users;
import com.authserver.util.ValidatorUtil;
import com.robi.util.CipherUtil;
import com.robi.util.JwtUtil;
import com.robi.util.MapUtil;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;

import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@PropertySource("config.properties")
@Service
public class UsersService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private UsersRepository usersRepo;
    private static Environment env;

    private static final String USER_JWT_VERSION;
    private static final Key USER_JWT_SIGN_KEY;
    private static final SecretKeySpec USER_JWT_AES_KEY;
    private static final Long USER_JWT_DEFAULT_DURATION_MS;

    static {
        // USER_JWT_VERSION
        USER_JWT_VERSION = env.getProperty("userJwt.jwtVersion");

        // USER_JWT_SIGN_KEY
        byte[] jwtHashKeyByte = null;
        jwtHashKeyByte = env.getProperty("userJwt.jwtHashKey").getBytes();

        if (jwtHashKeyByte.length != 32) { // JwtSecretKey값이 32Byte가 아니면, 패딩하거나 자름
            byte[] newJwtSecretKeyByte = new byte[32];
            System.arraycopy(jwtHashKeyByte, 0, newJwtSecretKeyByte, 0,
                    Math.min(jwtHashKeyByte.length, newJwtSecretKeyByte.length));
            jwtHashKeyByte = newJwtSecretKeyByte;
        }

        USER_JWT_SIGN_KEY = Keys.hmacShaKeyFor(jwtHashKeyByte);

        // USER_JWT_AES_KEY
        byte[] aes128KeyBytes = new byte[16];
        byte[] aesKeyBytesFromEnv = env.getProperty("userJwt.jwtAesKey").getBytes();
        System.arraycopy(aesKeyBytesFromEnv, 0, aes128KeyBytes, 0,
                         Math.min(aes128KeyBytes.length, aesKeyBytesFromEnv.length));
        USER_JWT_AES_KEY = new SecretKeySpec(aes128KeyBytes, "AES");

        // USER_JWT_DEFAULT_DURATION_MS
        USER_JWT_DEFAULT_DURATION_MS = Long.parseLong(env.getProperty("userJwt.jwtLifeMinDefault"));
    }

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

    public ApiResult issueUserJwt(String audience, String email, String password, Long duration) {
        // 파라미터 검사
        ApiResult paramRst = null;

        if (!(paramRst = ValidatorUtil.nullOrZeroLen("audience", audience)).getResult()) {
            return paramRst;
        }

        if (!(paramRst = ValidatorUtil.isEmail(email)).getResult()) {
            return paramRst;
        }

        if (!(paramRst = ValidatorUtil.nullOrZeroLen("password", password)).getResult()) {
            return paramRst;
        }
        
        if (duration == null) {
            duration = USER_JWT_DEFAULT_DURATION_MS;
        }

        // 회원정보 획득
        ApiResult selectUserRst = selectUserByKey("email", email);

        if (selectUserRst == null || !selectUserRst.getResult()) {
            logger.error("Fail to find user! (email:" + email + ")");
            return ApiResult.make(false, "회원 정보를 찾을 수 없습니다.");
        }

        // 비밀번호 검사
        ApiResult pwHashingRst = hashingPassword(password);

        if (pwHashingRst == null || !pwHashingRst.getResult()) {
            logger.error("Fail to hashing request password!");
            return pwHashingRst;
        }

        Users selectedUser = (Users) selectUserRst.getData("selectedUser");
        String hashedPassword = pwHashingRst.getDataAsStr("password");

        if (!selectedUser.getPassword().equals(hashedPassword)) {
            logger.error("Password NOT equals!");
            return ApiResult.make(false, "비밀번호가 일치하지 않습니다.");
        }

        // JWT 발급
        // [JWT]
        //  <Header>
        //   - setHeader(Map<String, Object>)
        //  <Claims>
        //   - setClaims(Map<String, Object>)
        //   - setId(String) : 'jti'
        //   - setSubject(String) : 'sub'
        //   - setAudience(String) : 'aud'
        //   - setIssuer(String) : 'iss'
        //   - setIssuedAt(String) : 'iat'
        //   - setExpiration(Date) : 'exp'
        //   - setNotBefore(Date) : 'nbf'
        long jwtExpiredTimeMs = System.currentTimeMillis() + duration;

        String rawUserJwt = JwtUtil.buildJwt(MapUtil.toMap("ver", USER_JWT_VERSION),
                                             MapUtil.toMap("sub", "dev4robi-user-jwt",
                                                           "aud", audience,
                                                           "iat", "dev4robi-auths",
                                                           "exp", new Date(jwtExpiredTimeMs)),
                                             USER_JWT_SIGN_KEY);

        if (rawUserJwt == null) {
            logger.error("'rawUserJwt' is null!");
            return ApiResult.make(false, "JWT생성중 오류가 발생했습니다.");
        }

        // 발급된 JWT 암호화
        byte[] cryptedUserJwt = CipherUtil.encrypt(CipherUtil.AES_CBC_PKCS5, rawUserJwt.getBytes(), USER_JWT_AES_KEY);
        if (cryptedUserJwt == null) {
            logger.error("'cryptedUserJwt' is null!");
            return ApiResult.make(false, "JWT암호화중 오류가 발생했습니다.");
        }

        // JWT URL인코딩 수행
        String base64UserJwt = Base64Utils.encodeToUrlSafeString(cryptedUserJwt);

        if (base64UserJwt == null) {
            logger.error("'base64UserJwt' is null!");
            return ApiResult.make(false, "JWT변환중 오류가 발생했습니다.");
        }

        logger.info("Issuing user jwt success! (email: " + email + ")");
        return ApiResult.make(true, MapUtil.toMap("userJwt", base64UserJwt));
    }

    public ApiResult validateUserJwt(String userJwt) {
        logger.error("아직 구현되지 않은 부분입니다.");
        return null;
    }
}