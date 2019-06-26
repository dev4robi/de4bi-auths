package com.authserver.controller;

import java.util.HashMap;
import java.util.Map;

import com.robi.oauth.GoogleOAuth;
import com.robi.util.HttpUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class MainController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping("/main")
    public ModelAndView main() {
        Map<String, String> obj = new HashMap<String, String>();
        
        GoogleOAuth oauth = new GoogleOAuth();
        String oauthUrl = oauth.makeUrlForCode("497284575180-0ottstk5ehodlic3siv6srf4usietg9v.apps.googleusercontent.com",
                                               "code", "email profile", "nonce123", "http://localhost:50000/oauth/google/code2token",
                                               "state123", "select_account", "wap", "loginHint", "offline", false, null, "*");

        obj.put("googleLoginUrl", oauthUrl);
        return new ModelAndView("main", obj);
    }

    @RequestMapping("/register")
    public ModelAndView registerGoogle() {
        return new ModelAndView("register");
    }
}