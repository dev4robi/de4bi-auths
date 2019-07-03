package com.authserver.controller;

import java.util.HashMap;
import java.util.Map;

import com.authserver.service.GoogleOAuthService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@PropertySource("classpath:config.properties")
@Controller
public class MainController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private GoogleOAuthService googleOauthSvc;

    @RequestMapping("/main")
    public ModelAndView main() {
        Map<String, Object> modelMap = new HashMap<String, Object>();

        // - Google Oauth ---------------------------------------------------------------
        String googleOauthCodeUrl = googleOauthSvc.makeCodeUrl();

        if ((googleOauthCodeUrl = googleOauthSvc.makeCodeUrl()) == null) {
            logger.error("'googleOauthCodeUrl' is null!");
        }
        
        modelMap.put("googleLoginUrl", googleOauthCodeUrl);

        // - Kakao Oauth ---------------------------------------------------------------
        String kakaoOauthCodeUrl = null;

        if (kakaoOauthCodeUrl == null) {
            logger.error("'kakaoOauthCodeUrl' is null!");
        }

        modelMap.put("kakaoLoginUrl", kakaoOauthCodeUrl);

        // - Naver Oauth ---------------------------------------------------------------
        String naverOauthCodeUrl = null;

        if (naverOauthCodeUrl == null) {
            logger.error("'naverOauthCodeUrl' is null!");
        }

        modelMap.put("naverLoginUrl", naverOauthCodeUrl);

        return new ModelAndView("main", modelMap); // main.jsp
    }

    @RequestMapping("/register")
    public ModelAndView registerGoogle() {
        return new ModelAndView("register");
    }
}