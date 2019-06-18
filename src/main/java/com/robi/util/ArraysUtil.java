package com.robi.util;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArraysUtil {
    // [Class private constants]
    private static final Logger logger = LoggerFactory.getLogger(ArraysUtil.class);

    // [Methods]
    /**
     * <p>입력된 Map<String, Object> 객체를 배열로 변환합니다.</p>
     * @param inMap : 배열로 반환할 Map.
     * @return key/value 순으로 변경된 배열. ([0]:key1, [1]:value1, [2]:key2, [3]:value2, ...)
     */
    public static Object[] toArray(Map<String, Object> inMap) {
        // 파라미터 검사
        if (inMap == null) {
            logger.error("'inMap is null!");
            return null;
        }
        
        // 홀수열에 key, 짝수열에 value 대입
        Object[] rtAry = new Object[inMap.size() * 2];
        Object val = null;
        int loopI = 0;
        
        for (String key : inMap.keySet()) {
            val = (Object)inMap.get(key);
            rtAry[loopI] = (Object)key;
            rtAry[loopI + 1] = val;
            loopI += 2;
        }
        
        return rtAry;
    }    
}
