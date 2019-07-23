package com.robi.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class LogUtil {
    // [Class private constants]
    private static final Logger logger = LoggerFactory.getLogger(LogUtil.class);
    private static final String LAYER_STR = "layer";
    private static final String TID_STR   = "tId";
    
    // [Class public constants]
    public static final String LAYER_SYS = "SYS";
    public static final String LAYER_CTR = "CTR";
    public static final String LAYER_SVC = "SVC";
    public static final String LAYER_MAP = "MAP";
    public static final String LAYER_MGR = "MGR";
    public static final String LAYER_UTL = "UTL";
    public static final String LAYER_ETC = "ETC";
    
    // [Methods]
    /**
     * <p>로그 계층(layer)을 변경하고 기존 계층을 반환합니다.</p>
     * @param layerStr : 새로운 계층 문자열.
     * <pre>
     * - {@link LAYER_SYS} : 시스템 [SYS]
     * - {@link LAYER_CTR} : 컨트롤러 [CTR]
     * - {@link LAYER_SVC} : 서비스 [SVC]
     * - {@link LAYER_MAP} : DB Mapper [MAP]
     * - {@link LAYER_MGR} : 매니저 [MGR]
     * - {@link LAYER_UTL} : 유틸 [UTL]
     * - {@link LAYER_ETC} : 기타 [ETC]
     * - 커스텀 문자열 : 사용자 정의 계층
     * @return 계층 변경전 사용하던 계층 문자열을 반환합니다.
     */
    public static String changeLogLayer(String layerStr) {
        String oldLayer = MDC.get(LAYER_STR);
        MDC.put(LAYER_STR, layerStr);
        return oldLayer;
    }

    /**
     * <p>로그 tId를 변경하고 기존 tiD를 반환합니다.</p>
     * @param tId : 새로운 tId 문자열.
     * @return tId 변경전 사용하던 tId문자열을 반환합니다.
     */
    public static String changeTid(String tId) {
        String oldTid = MDC.get(TID_STR);
        MDC.put(TID_STR, tId);
        return oldTid;
    }
}