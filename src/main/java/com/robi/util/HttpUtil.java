package com.robi.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpUtil {

    private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);
    private static final ResponseHandler<String> BASIC_RESPONSE_HANDLER = new BasicResponseHandler();

    public static int httpGet(String reqUrl,
                              Map<String, String> reqHeader,
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

        try {
            if (resBody != null && resBody.length > 0) {
                resBody[0] = BASIC_RESPONSE_HANDLER.handleResponse(response);
            }
        }
        catch (NullPointerException | IOException | UnsupportedOperationException e) {
            logger.error("Exception!", e);
            return HttpStatus.SC_INTERNAL_SERVER_ERROR;
        }

        return statusCode;
    }

    /**
     *  - Reference: https://www.baeldung.com/httpclient-post-http-request
     */
    public static int httpPost(String reqUrl,
                               Map<String, String> reqHeader, Map<String, String> reqBody,
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

        if (reqBody != null) {
            try {
                List<NameValuePair> params = new LinkedList<NameValuePair>();

                for (String key : reqBody.keySet()) {
                    params.add(new BasicNameValuePair(key, reqBody.get(key)));
                }

                postReq.setEntity(new UrlEncodedFormEntity(params));
            }
            catch (UnsupportedEncodingException e) {
                logger.error("Exception!", e);
                return -1;
            }
        }

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

        try {
            if (resBody != null && resBody.length > 0) {
                resBody[0] = BASIC_RESPONSE_HANDLER.handleResponse(response);
            }
        }
        catch (NullPointerException | IOException | UnsupportedOperationException e) {
            logger.error("Exception!", e);
            return HttpStatus.SC_INTERNAL_SERVER_ERROR;
        }

        return statusCode;
    }
}