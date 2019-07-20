package com.robi.util;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapUtil {

    private static final Logger logger = LoggerFactory.getLogger(MapUtil.class);

    public static Map<String, Object> toMap(Object... keyValAry) {
        if (keyValAry == null) {
            logger.error("'keyValAry' is null!");
            return null;
        }

        if (keyValAry.length % 2 != 0) {
            logger.error("'keyValAry's length is NOT even! (length:" + keyValAry.length + ")");
            return null;
        }

        String key = null;
        Object val = null;
        Map<String, Object> rtMap = new HashMap<String, Object>();

        for (int i = 0; i < keyValAry.length; i += 2) {
            key = keyValAry[i].toString();
            val = keyValAry[i + 1];
            rtMap.put(key, val);
        }

        return rtMap;
    }
}