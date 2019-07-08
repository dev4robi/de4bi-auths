package com.authserver.data;

import java.util.HashMap;
import java.util.Map;

public class ApiResult {

    private static final String KEY_RESULT  = "result";
    private static final String KEY_MESSAGE = "result_message";
    private static final String KEY_DATA    = "result_data";

    public static final String DEFAULT_RESULT_TRUE  = "SUCCESS";
    public static final String DEFAULT_RESULT_FALSE = "FAIL";
    public static final String DEFAULT_MESSAGE_OK   = "OK";

    private boolean result;
    private String message;
    private Map<String, Object> data;

    private ApiResult(boolean result, String message, Map<String, Object> data) {
        this.result = result;
        this.message = message;
        this.data = data;
    }

    public static ApiResult make(boolean result) {
        return make(result, DEFAULT_MESSAGE_OK, null);
    }

    public static ApiResult make(boolean result, String message) {
        return make(result, message, null);
    }

    public static ApiResult make(boolean result, String message, Map<String, Object> data) {
        return new ApiResult(result, message, data);
    }

    public boolean isSuccess() {
        return this.result;
    }

    public boolean getResult() {
        return this.result;
    }

    public String getMessage() {
        return this.message;
    }

    public Map<String, Object> getData() {
        return this.data;
    }

    public void addData(String key, Object value) {
        if (this.data == null) {
            this.data = new HashMap<String, Object>();
        }

        this.data.put(key, value);
    }

    public void addData(Map<String, Object> data) {
        if (this.data == null) {
            this.data = new HashMap<String, Object>();
        }

        this.data.putAll(data);
    }

    public Map<String, Object> toMap() {
        Map<String, Object> rtMap = new HashMap<String, Object>();
        rtMap.put(KEY_RESULT, this.result);
        rtMap.put(KEY_MESSAGE, this.message);
        rtMap.put(KEY_DATA, this.data);
        return rtMap;
    }
}