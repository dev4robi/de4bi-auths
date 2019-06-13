package com.authserver.data;

import java.util.Map;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.ToString;

@AllArgsConstructor(access = AccessLevel.PUBLIC)
@ToString
public class AuthServerApiResponse {
    
    public static final String KEY_RESULT_CODE      = "resultCode";
    public static final String KEY_RESULT_MSG       = "resultMsg";
    public static final String KEY_RESULT_DATA      = "resultDatas";

    public static final String VAL_TID_DEFAULT      = null;
    public static final String VAL_CODE_SUCCESS     = "00000";
    public static final String VAL_CODE_SYSFAIL     = "99999";
    public static final String VAL_MSG_DEFAULT      = null;
    
    private Map<String, Object> apiResponse;
    // 새로운 필드 추가 가능. 단 getter존재 시, 컨트롤러 API응답값에도 필드가 추가될수 있음에 유의!

    public Map<String, Object> getapiResponse(){
        return this.apiResponse;
    }

    public boolean checkResultCodeSuccess() {
        if (this.apiResponse == null) {
            return false;
        }

        String resultCode = this.apiResponse.get(KEY_RESULT_CODE).toString();

        if (resultCode == null) {
            return false;
        }

        return resultCode.equals(VAL_CODE_SUCCESS);
    }

    public void addResultData(Map<String, Object> addResultData) {
        if (this.apiResponse == null || addResultData == null) {
            return;
        }

        Map<String, Object> responseDataMap = (Map<String, Object>) this.apiResponse.get(KEY_RESULT_DATA);

        if (responseDataMap == null) {
            return;
        }

        responseDataMap.putAll(addResultData);
    }

    public Object getResultData(String key) {
        if (this.apiResponse == null) {
            return null;
        }

        Map<String, Object> responseDataMap = (Map<String, Object>) this.apiResponse.get(KEY_RESULT_DATA);

        if (responseDataMap == null) {
            return null;
        }

        return responseDataMap.get(key);
    }
}