package com.robi.util;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpUtil {

    private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);

    public static String httpGet(String reqUrl) {
        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet getReq = new HttpGet(reqUrl);

            getReq.addHeader("test-key", "test-val");

            HttpResponse response = client.execute(getReq);

            if (response.getStatusLine().getStatusCode() == 200) {
                ResponseHandler<String> handler = new BasicResponseHandler();
                String body = handler.handleResponse(response);
                logger.info(body);
                return body;
            }
            else {
                logger.error("response status not 200(OK)!");
            }
        }
        catch (IOException e) {
            logger.error("Exception!", e);
        }

        return null;
    }
}