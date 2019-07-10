package com.authserver.util;

import com.authserver.data.ApiResult;
import com.robi.util.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidatorUtil {

    private static final Logger logger = LoggerFactory.getLogger(ValidatorUtil.class);

    public static ApiResult nullOrZeroLen(String key, String value) {
        if (value == null) {
            logger.error("'" + key + "' is null!");
            return ApiResult.make(false, "'" + key + "' 값이 비었습니다.");
        }

        if (value.length() == 0) {
            logger.error("'" + key + "' is zero length!");
            return ApiResult.make(false, "'" + key + "' 값이 비었습니다.");
        }

        return ApiResult.make(true);
    }

    public static ApiResult arthimatic(String key, long value, long min, long max) {
        try {
            if (value < min) {
                throw new Exception();
            }
            
            if (value > max) {
                throw new Exception();
            }
        }
        catch (Exception e) {
            logger.error("'" + key + "'s NOT between " + min + " < " + value + " < " + max);
            return ApiResult.make(false, "'" + key + "' 는 " + min + "~" + max + "사이의 정수여야 합니다.");
        }

        return ApiResult.make(true);
    }

    public static ApiResult byteLen(String key, String value, int minLen, int maxLen) {
        ApiResult validationRst = null;

        if (!(validationRst = nullOrZeroLen(key, value)).getResult()) {
            return validationRst;
        }

        byte[] valueBytes = value.getBytes();

        try {
            if (valueBytes.length < minLen) {
                throw new Exception();
            }

            if (valueBytes.length > maxLen) {
                throw new Exception();
            }
        }
        catch (Exception e) {
            logger.error("'" + key + "'s byte length NOT between " + minLen + " < " + valueBytes.length + " < " + maxLen);
            return ApiResult.make(false, "'" + key + "' 는 " + minLen + "~" + maxLen + "바이트 사이여야 합니다.");
        }

        return ApiResult.make(true);
    }

    public static ApiResult strLen(String key, String value, int minLen, int maxLen) {
        ApiResult validationRst = null;
        
        if (!(validationRst = nullOrZeroLen(key, value)).getResult()) {
            return validationRst;
        }

        int valueLen = value.length();

        try {
            if (valueLen < minLen) {
                throw new Exception();
            }

            if (valueLen > maxLen) {
                throw new Exception();
            }
        }
        catch (Exception e) {
            logger.error("'" + key + "'s string length NOT between " + minLen + " < " + valueLen + " < " + maxLen);
            return ApiResult.make(false, "'" + key + "' 는 " + minLen + "~" + maxLen + " 자 사이여야 합니다.");
        }

        return ApiResult.make(true);
    }

    public static ApiResult isEmail(String email) {
        if (email == null) {
            logger.error("'email' is null!");
            return ApiResult.make(false, "이메일값이 비었습니다.");
        }

        int emailLen = email.length();

        if (email.length() > 128) {
            logger.error("'email' length over 128! (emailLen:" + emailLen + ")");
            return ApiResult.make(false, "이메일이 너무 깁니다. (" + emailLen + "/128)");
        }

        if (!StringUtil.isEmail(email)) {
            logger.error("'email' is NOT email-type! (email:" + email + ")");
            return ApiResult.make(false, "이메일 형식이 올바르지 않습니다.");
        }

        return ApiResult.make(true);
    }

    public static ApiResult isGender(String gender) {
        if (gender == null) {
            logger.error("'gender' is null!");
            return ApiResult.make(false, "성별값이 비었습니다.");
        }

        int genderLen = gender.length();

        if (genderLen != 1) {
            logger.error("'gender' length is NOT 1! (gender:" + gender + ")");
            return ApiResult.make(false, "성별값 길이가 올바르지 않습니다.");
        }

        char genderCh = gender.charAt(0);

        if (genderCh != 'M' && genderCh != 'F') { // male or female
            logger.error("'genderCh' is NOT 'M' or 'F'! (gender:" + gender + ")");
            return ApiResult.make(false, "성별값이 올바르지 않습니다.");
        }

        return ApiResult.make(true);
    }
}