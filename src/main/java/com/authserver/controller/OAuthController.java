package com.authserver.controller;

import com.authserver.data.ApiResult;
import com.authserver.service.GoogleOAuthService;
import com.authserver.service.UsersService;
import com.robi.util.CipherUtil;
import com.robi.util.RandomUtil;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Controller
public class OAuthController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private GoogleOAuthService googleOAuthSvc;
    private Environment env;

    @GetMapping("/oauth/google/code2token")
    public String oauthGoogleCode(@RequestParam("code") String code, @RequestParam("state") String state) {
        // code로부터 idToken획득
        String idToken = null;
        ApiResult codeApi = googleOAuthSvc.getIdTokenFromCode(code, state);

        if (codeApi != null && codeApi.getResult()) {
            idToken = codeApi.getDataAsStr("idToken");
        }

        if (idToken == null) {
            logger.error("'idToken' is null!");
            return null;
        }

        // idToken으로부터 email획득
        String email = null;
        ApiResult emailApi = googleOAuthSvc.getEmailFromIdToken(idToken);

        if (emailApi != null && emailApi.getResult()) {
            email = emailApi.getDataAsStr("email");
        }

        if (email == null) {
            logger.error("'email' is null!");
            return null;
        }

        // sign과 nonce생성
        ApiResult signNonceRst = googleOAuthSvc.genSignAndNonceForEmailValidation(email);

        if (signNonceRst == null || !signNonceRst.getResult()) {
            logger.error("'signNonceRst' is null or false!");
            return ("redirect:/error");
        }

        // 추가정보 입력페이지로 리다이렉션
        String sign = signNonceRst.getDataAsStr("sign");
        String nonce = signNonceRst.getDataAsStr("nonce");
        return ("redirect:/register?email=" + email + "&sign=" + sign + "&nonce=" + nonce);
    }
}