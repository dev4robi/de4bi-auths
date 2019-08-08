package com.authserver.service;

import java.security.Key;
import java.util.Date;
import java.util.Map;

import javax.annotation.PostConstruct;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.IncorrectClaimException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.MissingClaimException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

@PropertySource("config.properties")
@Service
public class UsersService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired private UsersRepository usersRepo;
    @Autowired private Environment env;

    private byte[] USER_PASSWORD_SERVER_SALT = null;
    private String AUTHS_AUDIENCE_NAME = null;
    private String USER_JWT_VERSION = null;
    private Key USER_JWT_SIGN_KEY = null;
    private SecretKeySpec USER_JWT_AES_KEY = null;
    private Long USER_JWT_DEFAULT_DURATION_MS = null;
    private Long USER_JWT_REQUEST_LIMIT_MS = null;

    @PostConstruct
    public void postConstruct() {
        // USER_PASSWORD_SERVER_SALT
        USER_PASSWORD_SERVER_SALT = env.getProperty("users.password.serverSalt").getBytes();

        // AUTHS_AUDIENCE_NAME
        AUTHS_AUDIENCE_NAME = env.getProperty("auths.audienceName");

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
        USER_JWT_DEFAULT_DURATION_MS = Long.parseLong(env.getProperty("userJwt.jwtDefaultLifeMin")) * 60000L;
        
        // USER_JWT_REQUEST_LIMIT_MS
        USER_JWT_REQUEST_LIMIT_MS = Long.parseLong(env.getProperty("userJwt.issueRequestLimitMs"));
    }

    /**
     * <p>DB에서 value값을 key로 사용하여 회원을 조회합니다.</p>
     * @param keyName : "email" || "id" || "nickname"
     * @param value : keyName에 해당하는 값
     * @return 존재하면 {@link Users} 'selectedUser'와 true, 존재하지 않으면 false
     */
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

        if (selectedUser == null) 
        {   
            logger.info("'selectedUser' is null!");
            return ApiResult.make(false, "존재하지 않는 회원입니다.");
        }

        logger.info("User select success! (selectedUser: " + selectedUser.toString() + ")");
        return ApiResult.make(true, null, MapUtil.toMap("selectedUser", selectedUser));
    }

    /**
     * <p>DB에서 userJwt를 검증후 회원을 조회합니다.</p>
     * @param userJwt : 인증서버에서 발급한 유효한 userJwt
     * @return 존재하면 "id","email","nickName","fullName","gender","dateOfBirth","accessLevel",  
                        "status","joinTime","lastLoginTime","accessibleTime"값들과 true,
                        존재하지 않으면 false
     */
    public ApiResult selectUserByJwt(String userJwt) {
        ApiResult paramValidationRst = ValidatorUtil.nullOrZeroLen("userJwt", userJwt);
        if (paramValidationRst == null || !paramValidationRst.getResult()) {
            logger.error("'paramValidationRst' is null or false! (paramValidationRst: " + paramValidationRst + ")");
            return paramValidationRst;
        }

        ApiResult jwtValidationRst = validateUserJwt(AUTHS_AUDIENCE_NAME, userJwt);
        if (jwtValidationRst == null || !jwtValidationRst.getResult()) {
            logger.error("'jwtValidationRst' is null or false! (jwtValidationRst: " + jwtValidationRst + ")");
            return jwtValidationRst;
        }

        String email = jwtValidationRst.getDataAsStr("email");
        ApiResult selectUserRst = selectUserByKey("email", email);
        if (selectUserRst == null || !selectUserRst.getResult()) {
            logger.error("'selectedUserRst' is null or false! (selectedUser: " + selectUserRst + ")");
            return selectUserRst;
        }

        Users selectedUser = (Users) selectUserRst.getData("selectedUser");
        if (selectedUser == null) {
            logger.error("'selectedUser' is null!");
            return ApiResult.make(false);
        }

        logger.info("User select success! (selectedUser: " + selectedUser.toString() + ")");
        return ApiResult.make(true, MapUtil.toMap(
            "id",              selectedUser.getId(),
            "email",           selectedUser.getEmail(),
            "nickName",        selectedUser.getNickname(),
            "fullName",        selectedUser.getFullName(),
            "gender",          selectedUser.getGender(),
            "dateOfBirth",     selectedUser.getDateOfBirth(),
            "accessLevel",     selectedUser.getAccessLevel(),
            "status",          selectedUser.getStatus(),
            "joinTime",        selectedUser.getJoinTime(),
            "lastLoginTime",   selectedUser.getLastLoginTime(),
            "accessibleTime",  selectedUser.getAccessibleTime())
        );
    }

    /**
     * <p>DB에 회원을 추가합니다.</p>
     * @param email : 회원 이메일 (UK)
     * @param password : 비밀번호 (NN)
     * @param nickname : 닉네임 (UK)
     * @param fullName : 이름 (NN)
     * @param gender : 성별 (NN)
     * @param dateOfBirth : 생년월일 (NN)
     * @return 추가성공시 true, 실패시 false
     */
    public ApiResult insertUser(String email, String password, String nickname,
                                String fullName, String gender, Long dateOfBirth) {
        // Param check
        ApiResult paramValidationRst = null;

        if (!(paramValidationRst = ValidatorUtil.isEmail(email)).getResult()) {
            logger.error("'email' vaildation failed!");
            return paramValidationRst;
        }

        if (!(paramValidationRst = ValidatorUtil.strLen("password", password, 8, 64)).getResult()) {
            logger.error("'password' vaildation failed!");
            return paramValidationRst;
        }

        if (!(paramValidationRst = ValidatorUtil.strLen("nickname", nickname, 4, 16)).getResult()) {
            logger.error("'nickname' vaildation failed!");
            return paramValidationRst;
        }

        if (!(paramValidationRst = ValidatorUtil.strLen("fullName", fullName, 2, 64)).getResult()) {
            logger.error("'fullName' vaildation failed!");
            return paramValidationRst;
        }

        if (!(paramValidationRst = ValidatorUtil.isGender(gender)).getResult()) {
            logger.error("'gender' vaildation failed!");
            return paramValidationRst;
        }

        if (!(paramValidationRst = ValidatorUtil.arthimatic("dateOfBirth", dateOfBirth, 0L, Long.MAX_VALUE)).getResult()) {
            logger.error("'dateOfBirth' vaildation failed!");
            return paramValidationRst;
        }

        // Password salting and hashing
        ApiResult hashingPasswordRst = hashingPassword(email, password);

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

    /**
     * <p>DB에 회원정보를 갱신합니다.</p>
     * @param userJwt : 회원 JWT
     * @param password : 비밀번호 (NN)
     * @param nickname : 닉네임 (UK)
     * @param fullName : 이름 (NN)
     * @param gender : 성별 (NN)
     * @param dateOfBirth : 생년월일 (NN)
     * @return 갱신성공시 true, 실패시 false
     */
    @Transactional
    public ApiResult updateUser(String userJwt, String password, String nickname,
                                String fullName, String gender, Long dateOfBirth) {
        // Validate UserJwt
        ApiResult userJwtValidateRst = validateUserJwt(AUTHS_AUDIENCE_NAME, userJwt);

        if (userJwtValidateRst == null || !userJwtValidateRst.getResult()) {
            logger.error("'userJwtValidateRst' is null or false!");
            return userJwtValidateRst;
        }

        // Param check
        String email = userJwtValidateRst.getDataAsStr("email");
        ApiResult paramValidationRst = null;

        if (!(paramValidationRst = ValidatorUtil.isEmail(email)).getResult()) {
            logger.error("'email' vaildation failed!");
            return paramValidationRst;
        }

        if (!(paramValidationRst = ValidatorUtil.strLen("password", password, 8, 32)).getResult()) {
            logger.error("'password' vaildation failed!");
            return paramValidationRst;
        }

        if (!(paramValidationRst = ValidatorUtil.strLen("nickname", nickname, 4, 16)).getResult()) {
            logger.error("'nickname' vaildation failed!");
            return paramValidationRst;
        }

        if (!(paramValidationRst = ValidatorUtil.strLen("fullName", fullName, 2, 64)).getResult()) {
            logger.error("'fullName' vaildation failed!");
            return paramValidationRst;
        }

        if (!(paramValidationRst = ValidatorUtil.isGender(gender)).getResult()) {
            logger.error("'gender' vaildation failed!");
            return paramValidationRst;
        }

        if (!(paramValidationRst = ValidatorUtil.arthimatic("dateOfBirth", dateOfBirth, 0L, Long.MAX_VALUE)).getResult()) {
            logger.error("'dateOfBirth' vaildation failed!");
            return paramValidationRst;
        }

        // Password salting and hashing
        ApiResult hashingPasswordRst = hashingPassword(email, password);

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

    /**
     * <p>DB에 회원의 마지막 로그인 시간을 갱신합니다.</p>
     * @param selectedUser : selectUserByKey()로 선택된 회원 객체
     * @param lastLoginTime : 마지막으로 로그인한 시간 (NN)
     * @return 갱신성공시 true, 실패시 false
     */
    @Transactional
    public ApiResult updateUserLastLoginTime(Users selectedUser, long lastLoginTime) {
        if (selectedUser == null) {
            logger.error("'selectedUser' is null!");
            return ApiResult.make(false);
        }

        // JPA - update users
        try {
            selectedUser.setLastLoginTime(lastLoginTime);
            
            if (usersRepo.save(selectedUser) == null) {
                logger.error("save() for update return null!");
                throw new Exception();
            }
        }
        catch (Exception e) {
            logger.error("JPA Exception!", e);
            return ApiResult.make(false, "회원정보 DB수정중 오류가 발생했습니다.");
        }

        logger.info("JPA - update success! (selectedUser:" + selectedUser.toString() + ")");
        return ApiResult.make(true);
    }

    /**
     * <p>DB에 회원정보를 삭제(갱신)합니다.</p>
     * @param userJwt : 회원 JWT
     * @return 삭제성공시 true, 실패시 false
     */
    @Transactional
    public ApiResult deleteUser(String userJwt) {
        // Validate UserJwt
        ApiResult userJwtValidateRst = validateUserJwt(AUTHS_AUDIENCE_NAME, userJwt);

        if (userJwtValidateRst == null || !userJwtValidateRst.getResult()) {
            logger.error("'userJwtValidateRst' is null or false!");
            return userJwtValidateRst;
        }

        // Param check
        String email = userJwtValidateRst.getDataAsStr("email");
        ApiResult paramValidationRst = null;

        if (!(paramValidationRst = ValidatorUtil.isEmail(email)).getResult()) {
            logger.error("'email' validation failed!");
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

    /**
     * <p>회원의 비밀번호를 SALT-HASHING 수행합니다.
     * <pre>해싱 알고리즘은 다음과 같습니다.
     *  1) Client로부터 'users.password.clientSalt' SALT-SHA256해싱된 'passwordLv1' 획득
     *     (http사용 시 최소한의 방어를 위함)
     *  2) Server에서 'email' + 'passwordLv1' 값에 'users.password.serverSalt'값을 Salt로 사용한 SALT-SHA256 'password' 생성</p></pre>
     * @param password : 사용자로부터 전달받은 해싱된 비밀번호
     * @return 해싱성공시 {@link String} 'password'값과 true, 실패시 false
     */
    private ApiResult hashingPassword(String email, String passwordLv1) {
        String password = null;

        try {
            String emailWithPasswordLv1 = email + passwordLv1;
            byte[] passwordBytes = CipherUtil.hashing(CipherUtil.SHA256, emailWithPasswordLv1.getBytes(), USER_PASSWORD_SERVER_SALT);

            if (passwordBytes == null) {
                logger.error("'passwordBytes' is null!");
                throw new Exception();
            }

            password = Hex.encodeHexString(passwordBytes);
        }
        catch (Exception e) {
            logger.error("Exception while password hashing!", e);
            return ApiResult.make(false, "비밀번호 암호화중 오류가 발생했습니다.");
        }

        return ApiResult.make(true, null, MapUtil.toMap("password", password));
    }

    /**
     * <p>회원이 정상적으로 서비스사용 권한이 있는지 확인합니다.</p>
     * @param valiateTargetUser : 확인할 회원 객체
     * @return 권한확인 성공시 true, 실패시 false
     */
    public ApiResult validateUserServiceAccesibble(Users valiateTargetUser) {
        // 파라미터 검사
        if (valiateTargetUser == null) {
            logger.error("'valiateTargetUser' is null!");
            return ApiResult.make(false);
        }

        // 유저상태 검사
        String email = valiateTargetUser.getEmail();
        UsersStatus validateUserStatus = valiateTargetUser.getStatus();

        if (validateUserStatus.equals(UsersStatus.SLEEPING)) {
            logger.info("Validated user '" + email + "' status is 'SLEEPING'!");
            return ApiResult.make(false, "휴면중인 회원입니다.");
        }
        else if (validateUserStatus.equals(UsersStatus.DEREGISTERED)) {
            logger.info("Validated user '" + email + "' status is 'DEREGISTERED'!");
            return ApiResult.make(false, "탈퇴한 회원입니다.");
        }
        else if (validateUserStatus.equals(UsersStatus.BLACKLIST)) {
            logger.info("Validated user '" + email + "' status is 'BLACKLIST'!");
            return ApiResult.make(false, "블랙리스트 처리된 회원입니다.");
        }

        // 서비스 이용가능시점 검사
        long curTime = System.currentTimeMillis();
        long serviceAccessibleTime = valiateTargetUser.getAccessibleTime();

        if (serviceAccessibleTime > curTime) {
            logger.info("Validated user '" + email + "' NOT reached accessible time! (serviceAccessibleTime: " +
                        serviceAccessibleTime + " > curTime: " + curTime + ")");
            return ApiResult.make(false, "서비스 이용가능까지 " + (serviceAccessibleTime - curTime) + "ms 남은 회원입니다.");
        }

        return ApiResult.make(true);
    }

    /**
     * <p>회원 JWT를 발급합니다.</p>
     * @param audience : 발급요청한 대상(서비스)의 식별자
     * @param email : 회원 이메일
     * @param password : 회원 비밀번호
     * @param duration : 토큰 지속시간 (분 단위)
     * @return 발급 성공시 {@link String} 'userJwt'값과 true, 실패시 false
     */
    @Transactional
    public ApiResult issueUserJwt(String audience, String email, String password, Long duration) {
        // 파라미터 검사
        ApiResult paramRst = null;

        if (!(paramRst = ValidatorUtil.nullOrZeroLen("audience", audience)).getResult()) {
            logger.error("'audience' validation failed!");
            return paramRst;
        }

        if (!(paramRst = ValidatorUtil.isEmail(email)).getResult()) {
            logger.error("'email' validation failed!");
            return paramRst;
        }

        if (!(paramRst = ValidatorUtil.nullOrZeroLen("password", password)).getResult()) {
            logger.error("'password' validation failed!");
            return paramRst;
        }
        
        if (duration == null | duration == 0L) {
            duration = USER_JWT_DEFAULT_DURATION_MS;
        }
        else {
            duration *= 1000L;
        }

        // 회원정보 획득
        ApiResult selectUserRst = selectUserByKey("email", email);

        if (selectUserRst == null || !selectUserRst.getResult()) {
            logger.error("Fail to find user! (email:" + email + ")");
            return ApiResult.make(false, "회원 정보를 찾을 수 없습니다.");
        }

        // 마지막 발급시간 검사
        final Users selectedUser = (Users) selectUserRst.getData("selectedUser");
        long curTime = System.currentTimeMillis();
        long lastLoginTryDeltaMs =  curTime - selectedUser.getLastLoginTime();

        if (lastLoginTryDeltaMs < USER_JWT_REQUEST_LIMIT_MS) {
            logger.error("'userJwt' request too fast! (lastLoginTryDeltaMs: " + lastLoginTryDeltaMs + ")");
            return ApiResult.make(false, "너무 짧은 주기로 userJwt를 요청하고 있습니다.");
        }

        // 마지막 로그인시간 갱신
        ApiResult usersUpdateRst = updateUserLastLoginTime(selectedUser, curTime);

        if (usersUpdateRst == null || !usersUpdateRst.getResult()) {
            logger.error("'usersUpdateRst' is null or false! (usersUpdateRst: " + usersUpdateRst + ")");
            return usersUpdateRst;
        }

        // 비밀번호 검사
        ApiResult pwHashingRst = hashingPassword(email, password);
        String hashedPassword = pwHashingRst.getDataAsStr("password");

        if (pwHashingRst == null || !pwHashingRst.getResult()) {
            logger.error("Fail to hashing request password!");
            return pwHashingRst;
        }

        if (!selectedUser.getPassword().equals(hashedPassword)) {
            logger.error("Password NOT equals!");
            return ApiResult.make(false, "비밀번호가 일치하지 않습니다.");
        }
        
        // 회원 서비스 접근허용 검사
        ApiResult validateUserRst = validateUserServiceAccesibble(selectedUser);

        if (validateUserRst == null || !validateUserRst.getResult()) {
            logger.error("'validateUserRst' is null or result false!");
            return validateUserRst;
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
                                                           "iat", AUTHS_AUDIENCE_NAME,
                                                           "exp", new Date(jwtExpiredTimeMs),
                                                           "email", email),
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

    /**
     * <p>발급한 회원 JWT를 검증합니다.</p>
     * @param audience : 검증요청한 대상(서비스)의 식별자 (발급요청자와 일치해야 함)
     * @param userJwt : 검증할 회원 JWT
     * @return 검증 성공시 {@link String} 'email'값과 true, 실패시 false
     */
    public ApiResult validateUserJwt(String audience, String userJwt) {
        // 파라미터 검사
        ApiResult paramRst = null;

        if (!(paramRst = ValidatorUtil.nullOrZeroLen("audience", audience)).getResult()) {
            logger.error("'audience' validation failed!");
            return paramRst;
        }

        if (!(paramRst = ValidatorUtil.nullOrZeroLen("userJwt", userJwt)).getResult()) {
            logger.error("'userJwt' validation failed!");
            return paramRst;
        } 

        // URL디코딩 수행
        byte[] cryptedUserJwt = Base64Utils.decodeFromUrlSafeString(userJwt);
        
        if (cryptedUserJwt == null) {
            logger.error("'cryptedUserJwt' is null!");
            return ApiResult.make(false, "JWT변환중 오류가 발생했습니다.");
        }

        // 복호화 수행
        byte[] rawUserJwt = CipherUtil.decrypt(CipherUtil.AES_CBC_PKCS5, cryptedUserJwt, USER_JWT_AES_KEY);

        if (rawUserJwt == null) {
            logger.error("'rawUserJwt' is null!");
            return ApiResult.make(false, "JWT복호화중 오류가 발생했습니다.");
        }

        // JWT 파싱
        Map<String, Object> userJwtMap = null;
        
        try {   
            userJwtMap = JwtUtil.parseJwt(new String(rawUserJwt), 
                                          MapUtil.toMap("sub", "dev4robi-user-jwt"), 
                                          USER_JWT_SIGN_KEY);
        }
        catch (IllegalArgumentException e) {
            // jwtStr가 null이거나 길이가 0인 경우
            logger.error("Exception! 'rawUserJwt' is null or zero length! (rawUserJwt: " + rawUserJwt + ")");
            return ApiResult.make(false, "userJwt값이 비었습니다.");
        }
        catch (MalformedJwtException e) {
            // jwtStr가 JWT토큰 포멧이 아닌경우
            logger.error("Exception! 'rawUserJwt' type is NOT JWT! (rawUserJwt: " + rawUserJwt + ")");
            return ApiResult.make(false, "userJwt가 올바른 JWT포멧이 아닙니다.");
        }
        catch (ExpiredJwtException e){
            // 토큰 유효기간이 만료된 경우
            logger.error("Exception! JWT is EXPIRED! (rawUserJwt: " + rawUserJwt + ")");
            return ApiResult.make(false, "userJwt가 만료되었습니다.");
        }
        catch (SignatureException e) {
            // 서명검사 오류가 발생한 경우
            logger.error("Exception! JWT is SIGN UNVALID! (rawUserJwt: " + rawUserJwt + ")");
            return ApiResult.make(false, "userJwt의 서명이 올바르지 않습니다.");
        }
        catch (MissingClaimException e) {
            // jwtRequried의 key값이 Claims에 존재하지 않는 경우
            logger.error("Exception! JWT requried claim NOT exist! (rawUserJwt: " + rawUserJwt + ")");
            return ApiResult.make(false, "userJwt 바디에 필요한 필수값이 비었습니다.");
        }
        catch (IncorrectClaimException e) {
            // jwtRequried의 key값에 해당하는 value가 불일치
            logger.error("Exception! JWT requried claim NOT matched! (rawUserJwt: " + rawUserJwt + ")");
            return ApiResult.make(false, "userJwt 바디에 필요한 필수값 데이터가 올바르지 않습니다.");
        }
        catch (Exception e) {
            logger.error("Exception while parsing JWT!", e);
            return ApiResult.make(false, "JWT파싱중 오류가 발생했습니다.");
        }

        if (userJwtMap == null) {
            logger.error("'userJwtMap' is null!");
            return ApiResult.make(false, "JWT파싱중 오류가 발생했습니다.");
        }

        // Jwt발급자와 검증자 서비스명 비교
        String originIssure = (String) userJwtMap.get("aud");
        if (originIssure == null) {
            logger.error("'originIssure' is null!");
            return ApiResult.make(false, "JWT에 발급자가 포함되어있지 않습니다.");
        }
        else if (!originIssure.equals(audience)) {
            logger.error("'originIssure' NOT equals 'audience'! (originIssure:" + originIssure + " != audience: " + audience);
            return ApiResult.make(false, "JWT를 발급한 서비스와 검증하려는 서비스가 일치하지 않습니다.");
        }

        // 회원정보 획득
        String email = (String) userJwtMap.get("email");
        ApiResult selectUserRst = selectUserByKey("email", email);

        if (selectUserRst == null || !selectUserRst.getResult()) {
            logger.error("Fail to find user! (email:" + email + ")");
            return ApiResult.make(false, "회원 정보를 찾을 수 없습니다.");
        }

        // 회원 서비스 접근허용 검사
        Users validatedUser = (Users) selectUserRst.getData("selectedUser");
        ApiResult validateUserRst = validateUserServiceAccesibble(validatedUser);

        if (validateUserRst == null || !validateUserRst.getResult()) {
            logger.error("'validateUserRst' is null or result false!");
            return validateUserRst;
        }

        // 검증 성공
        logger.info("'UserJwt '" + userJwt + "' for '" + audience + "' is VALIDATED! (email:" + email + ")");
        return ApiResult.make(true, MapUtil.toMap("email", email));
    }
}