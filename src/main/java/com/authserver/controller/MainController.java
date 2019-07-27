package com.authserver.controller;

import java.util.HashMap;
import java.util.Map;

import com.authserver.data.ApiResult;
import com.authserver.service.GoogleOAuthService;
import com.robi.util.MapUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@PropertySource("classpath:config.properties")
@Controller
public class MainController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private GoogleOAuthService googleOauthSvc;

    @RequestMapping("/main")
    public ModelAndView mainPage() {
        Map<String, Object> modelMap = new HashMap<String, Object>();

        // - Google Oauth ---------------------------------------------------------------
        ApiResult codeUrlRst = googleOauthSvc.makeCodeUrl();
        String googleOauthCodeUrl = null;

        if (codeUrlRst != null && codeUrlRst.getResult()) {
            googleOauthCodeUrl = codeUrlRst.getDataAsStr("codeUrl");
        }
        
        modelMap.put("googleLoginUrl", googleOauthCodeUrl);
        codeUrlRst = null;

        // - Kakao Oauth ---------------------------------------------------------------
        // codeUrlRst = kakaoOauthSvc.makeCodeUrl();
        String kakaoOauthCodeUrl = null;

        if (codeUrlRst != null && codeUrlRst.getResult()) {
            kakaoOauthCodeUrl = codeUrlRst.getDataAsStr("codeUrl");
        }

        modelMap.put("kakaoLoginUrl", kakaoOauthCodeUrl);

        // - Naver Oauth ---------------------------------------------------------------
        // codeUrlRst = naverOauthSvc.makeCodeUrl();
        String naverOauthCodeUrl = null;

        if (codeUrlRst != null && codeUrlRst.getResult()) {
            naverOauthCodeUrl = codeUrlRst.getDataAsStr("codeUrl");
        }

        modelMap.put("naverLoginUrl", naverOauthCodeUrl);

        // - Return view ---------------------------------------------------------------
        return new ModelAndView("main", modelMap); // main.jsp
    }

    @RequestMapping("/register")
    public ModelAndView registerPage(
        @RequestParam String email, @RequestParam String sign, @RequestParam String nonce) {
        return new ModelAndView("register", MapUtil.toMap("email", email, "sign", sign, "nonce", nonce));
    }

    @RequestMapping("/errors")
    public ModelAndView errorPage(
        @RequestParam(required = false) String alertMsg, @RequestParam(required = false) String errorMsg) {
        return new ModelAndView("error", MapUtil.toMap("alertMsg", alertMsg, "errorMsg", errorMsg));
    }
}