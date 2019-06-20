package com.robi.util;

import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringUtil {
    // [Class private constants]
    private static final Logger logger = LoggerFactory.getLogger(StringUtil.class);
    private static final Pattern EMAIL_PATTERN = Pattern.compile("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])");

    // [Methods]
    /**
     * <p>문자열이 Email형식인지 확인.</p>
     * @param email : 확인할 문자열.
     * @return 이메일인 경우 true, 아닌 경우 false 반환.
     */
    public static boolean isEmail(String email) {
        return EMAIL_PATTERN.matcher(email).find();
    }

    /**
     * <p>입력된 Object[] 객체의 내용을 연결하여 문자열로 반환합니다.</p>
     * @param inArary : 문자열로 반환할 배열.
     * @return 문자열로 연결된 배열의 내용.
     */
    public static String arrayToString(Object[] inArary) {
        StringBuilder rtSb = new StringBuilder();
        String delimStr = ", ";
        
        for (Object obj : inArary) {
            rtSb.append(obj.toString()).append(delimStr);
        }
        
        rtSb.setLength(rtSb.length() - delimStr.length());
        return rtSb.toString();
    }

    /**
     * <p>입력된 Object... 객체의 내용을 연결하여 (key1:val1, key2:val2)형태의 문자열로 반환합니다.</p>
     * <p>입력하는 Object... 객체의 [홀수]열에는 key값이, [짝수]열에는 val값이 들어가야 합니다.</p>
     * @param keyValArray : key[0],val[1],... 로 구성된 배열.
     * @return (key1:val1, key2:val2)형태 문자열.
     */
    public static String keyValArrayToString(Object... keyValArray) {
        if (keyValArray == null) {
            return null;
        }

        StringBuilder rtSb = new StringBuilder();
        String columnStr = ":";
        String delimStr = ", ";
        int keyValArrayLen = keyValArray.length;
        String key = null, val = null;

        if (keyValArrayLen % 2 != 0) {
            return null;
        }
        
        for (int i = 0; i < keyValArrayLen; i += 2) {
            key = (String) keyValArray[i];
            val = keyValArray[i + 1].toString();
            rtSb.append(key).append(columnStr).append(val).append(delimStr);
        }

        rtSb.setLength(rtSb.length() - delimStr.length());
        return rtSb.toString();
    }
}
