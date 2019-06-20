package com.robi.util;

import java.io.IOException;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpUtil {

    private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);
    private static final ResponseHandler<String> BASIC_RESPONSE_HANDLER = new BasicResponseHandler();

    public static int httpGet(String reqUrl, Map<String, Object> reqHeader,
                              Map<String, String> resHeader, String[] resBody) {
        if (reqUrl == null || reqUrl.length() == 0) {
            logger.error("'reqUrl' is null or zero length! (reqUrl:" + reqUrl + ")");
            return -1;
        }

        HttpClient client = HttpClientBuilder.create().build();
        HttpGet getReq = new HttpGet(reqUrl);

        if (reqHeader != null) {
            Object valueObj = null;

            for (String key : reqHeader.keySet()) {
                valueObj = reqHeader.get(key);
                getReq.addHeader(key, (valueObj == null ? null : valueObj.toString()));
            }
        }

        HttpResponse response = null;

        try {
            response = client.execute(getReq);
        }
        catch (IOException e) {
            logger.error("Exception!", e);
            return HttpStatus.SC_INTERNAL_SERVER_ERROR;
        }
        
        if (resHeader != null) {
            Header[] resHeaders = response.getAllHeaders();
            
            for (int i = 0 ; i < resHeaders.length; ++i) {
                resHeader.put(resHeaders[i].getName(), resHeaders[i].getValue());
            }
        }           
        
        int statusCode = response.getStatusLine().getStatusCode();

        if (statusCode == HttpStatus.SC_OK) {
            try {
                if (resBody != null && resBody.length > 0) {
                    resBody[0] = BASIC_RESPONSE_HANDLER.handleResponse(response);
                }
            }
            catch (NullPointerException | IOException | UnsupportedOperationException e) {
                logger.error("Exception!", e);
                return HttpStatus.SC_INTERNAL_SERVER_ERROR;
            }
        }
        else {
            logger.error("Response status not 200(OK)! (reqUrl:" + reqUrl + ")");
        }

        return statusCode;
    }

    public static int httpPost(String reqUrl, Map<String, Object> reqHeader, String reqBody,
                               Map<String, String> resHeader, String[] resBody) {
        if (reqUrl == null || reqUrl.length() == 0) {
            logger.error("'reqUrl' is null or zero length! (reqUrl:" + reqUrl + ")");
            return -1;
        }

        HttpClient client = HttpClientBuilder.create().build();
        HttpPost postReq = new HttpPost(reqUrl);

        if (reqHeader != null) {
            Object valueObj = null;

            for (String key : reqHeader.keySet()) {
                valueObj = reqHeader.get(key);
                postReq.addHeader(key, (valueObj == null ? null : valueObj.toString()));
            }
        }

        // https://www.baeldung.com/httpclient-post-http-request 이거 참고해서 post 만들기부터 시작...! @@

        HttpResponse response = null;

        try {
            response = client.execute(postReq);
        }
        catch (IOException e) {
            logger.error("Exception!", e);
            return HttpStatus.SC_INTERNAL_SERVER_ERROR;
        }
        
        if (resHeader != null) {
            Header[] resHeaders = response.getAllHeaders();
            
            for (int i = 0 ; i < resHeaders.length; ++i) {
                resHeader.put(resHeaders[i].getName(), resHeaders[i].getValue());
            }
        }           
        
        int statusCode = response.getStatusLine().getStatusCode();

        if (statusCode == HttpStatus.SC_OK) {
            try {
                if (resBody != null && resBody.length > 0) {
                    resBody[0] = BASIC_RESPONSE_HANDLER.handleResponse(response);
                }
            }
            catch (NullPointerException | IOException | UnsupportedOperationException e) {
                logger.error("Exception!", e);
                return HttpStatus.SC_INTERNAL_SERVER_ERROR;
            }
        }
        else {
            logger.error("Response status not 200(OK)! (reqUrl:" + reqUrl + ")");
        }

        return statusCode;
    }
}