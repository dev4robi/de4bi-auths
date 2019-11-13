package com.authserver.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import com.robi.data.ApiResult;
import com.robi.oauth.GoogleOAuth;
import com.robi.util.CipherUtil;
import com.robi.util.MapUtil;
import com.robi.util.RandomUtil;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@PropertySource("classpath:config.properties")
@Service
public class GoogleOAuthService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private Environment env;
    private GoogleOAuth googleOAuth;

    // OAuth URL 생성
    public ApiResult makeCodeUrl() {
        String nonce = RandomUtil.genRandomStr(16, RandomUtil.ALPHABET | RandomUtil.NUMERIC);
        String clientId = env.getProperty("auth.google.clientId");
        String redirectUrl = env.getProperty("auth.google.tokenRedirectionUrl");
        String redirectionSignKey = env.getProperty("auth.google.redirectionSignKey");

        MessageDigest md = null;

        try {
            md = MessageDigest.getInstance(MessageDigestAlgorithms.SHA_256);
        }
        catch (NoSuchAlgorithmException e) {
            logger.error("Exception!", e);
            return ApiResult.make(false);
        }

        md.update((clientId + nonce + redirectionSignKey).getBytes());

        String redirectionSign = Hex.encodeHexString(md.digest());
        String state = "redirectionSign=" + redirectionSign;
        String rtUrl = googleOAuth.makeUrlForCode(clientId, "code", "email", nonce, redirectUrl,
                                                  state, "select_account", "wap", "loginHint", "offline", 
                                                  false, null, "*");
        
        return ApiResult.make(true, MapUtil.toMap("codeUrl", rtUrl));
    }

    // state로부터 sign검사, code로부터 idToken획득
    public ApiResult getIdTokenFromCode(String code, String state) {
        if (code == null || code.length() == 0) {
            logger.error("'code' is null or zero length! (code:" + code + ")");
            return ApiResult.make(false);
        }

        if (state == null || state.length() == 0) {
            logger.error("'state' is null or zero length! (state:" + state + ")");
            return ApiResult.make(false);
        }

        // 응답값 해시서명 검사
        final String redirectionSignStr = state.substring("redirectionSign=".length());
        final String clientId = env.getProperty("auth.google.clientId");
        final String redirectionSignKey = env.getProperty("auth.google.redirectionSignKey");
        String signStr = (clientId + redirectionSignKey);
        MessageDigest md = null;

        try {
            md = MessageDigest.getInstance(MessageDigestAlgorithms.SHA_256);
        }
        catch (NoSuchAlgorithmException e) {
            logger.error("Exception!", e);
            return ApiResult.make(false);
        }

        md.update(signStr.getBytes());
        signStr = Hex.encodeHexString(md.digest());

        if (!redirectionSignStr.equals(signStr)) {
            // 지금 당장은 서명검사 실패여도 프리패스시킴.
            // 단, 실제로 서비스한다면 이 페이지로 공격이 들어올 가능성이 있다.
            // 서명검사를 할수있는 방안을 연구해봐야 함.
            // 1. 파일DB 사용
            // 2. sqlite 사용
            // 3. 인메모리DB 사용
            // 4. ?
            //logger.error("Invaild sign! (From:" + redirectionSignStr + " != Gen:" + signStr + ")");
            //return null;
        }

        final String clientSecret = env.getProperty("auth.google.clientSecret");
        final String tokenRequestUrl = env.getProperty("auth.google.tokenRedirectionUrl");
        String rtIdToken = null;
        
        if ((rtIdToken = googleOAuth.getIdToken(code, clientId, clientSecret, tokenRequestUrl, "authorization_code")) == null) {
            logger.error("'rtIdToken' is null!");
            return ApiResult.make(false);
        }

        return ApiResult.make(true, MapUtil.toMap("idToken", rtIdToken));
    }

    // idToken(JWT)를 식별, 파싱하여 사용자 이메일값을 반환
    public ApiResult getEmailFromIdToken(String idToken) {
        if (idToken == null || idToken.length() == 0) {
            logger.error("'idToken' is null or zero length! (idToken:" + idToken + ")");
            return ApiResult.make(false);
        }

        Map<String, Object> parsedJwtMap = googleOAuth.verifyAndParseIdTokenJwt(idToken);

        if (parsedJwtMap == null) {
            logger.error("'parsedJwtMap' is null!");
            return ApiResult.make(false);
        }

        Object emailObj = parsedJwtMap.get("email");
        return (emailObj != null ? ApiResult.make(true, MapUtil.toMap("email", emailObj.toString())) : ApiResult.make(false));
    }

    // 가입페이지에서 이메일 변조를 방지하기 위해 생성하는 sign값
    public ApiResult genSignAndNonceForEmailValidation(String email) {
        if (email == null) {
            logger.error("'email' is null!");
            return ApiResult.make(false);
        }

        // nonce생성
        int nonceLen = 16;
        String nonce = RandomUtil.genRandomStr(nonceLen, RandomUtil.ALPHABET | RandomUtil.NUMERIC);

        // sign생성
        String sign = Hex.encodeHexString(CipherUtil.hashing(CipherUtil.SHA256, (email + nonce.substring(0, nonceLen / 2)).getBytes(), env.getProperty("register.email.salt").getBytes()));
        return ApiResult.make(true, MapUtil.toMap("sign", sign, "nonce", nonce));
    }
}