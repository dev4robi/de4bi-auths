package com.authserver.controller;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Map;

import com.robi.oauth.GoogleOAuth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OAuthRestController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @GetMapping("/oauth/google/code2token")
    public Map<String, Object> oauthGoogleCode(@RequestParam("code") String code) {
        if (code == null || code.length() == 0) {
            logger.error("'code' is null or zero length! (code:" + code + ")");
            return null;
        }

        Map<String, Object> rtMap = null;
        GoogleOAuth oauth = new GoogleOAuth();
        if ((rtMap = oauth.getIdToken(code, "497284575180-0ottstk5ehodlic3siv6srf4usietg9v.apps.googleusercontent.com", 
                                      "ZNVLQPGYA7_stIy6c0LvFb2s", "http://localhost:50000/oauth/google/code2token", 
                                      "authorization_code")) == null) {
            return null;
        }

        return rtMap;
    }
}