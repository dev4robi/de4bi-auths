package com.authserver.controller;

import com.robi.data.ApiResult;
import com.authserver.data.jpa.enums.UsersStatus;
import com.authserver.data.jpa.table.Users;
import com.authserver.service.GoogleOAuthService;
import com.authserver.service.UsersService;
import com.robi.util.MapUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Controller
public class OAuthController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private GoogleOAuthService googleOAuthSvc;
    private UsersService usersSvc;

    @GetMapping("/oauth/google/code2token")
    public ModelAndView oauthGoogleCode(@RequestParam("code") String code, @RequestParam("state") String state) {
        // code로부터 idToken획득
        String idToken = null;
        ApiResult codeApi = googleOAuthSvc.getIdTokenFromCode(code, state);

        if (codeApi != null && codeApi.getResult()) {
            idToken = codeApi.getDataAsStr("idToken");
        }

        if (idToken == null) {
            logger.error("'idToken' is null!");
            return new ModelAndView("redirect:/errors", MapUtil.toMap("errorMsg", "올바르지 않은 토큰입니다."));
        }

        // idToken으로부터 email획득
        String email = null;
        ApiResult emailApi = googleOAuthSvc.getEmailFromIdToken(idToken);

        if (emailApi != null && emailApi.getResult()) {
            email = emailApi.getDataAsStr("email");
        }

        if (email == null) {
            logger.error("'email' is null!");
            return new ModelAndView("redirect:/errors", MapUtil.toMap("errorMsg", "이메일값이 비었습니다."));
        }

        // 기존 가입여부 확인
        ApiResult existedUser = usersSvc.selectUserByKey("email", email);
        
        if (existedUser.getResult()) { // 가입된 회원
            Users seletedUser = (Users) existedUser.getData("selectedUser");
            
            if (!seletedUser.getStatus().equals(UsersStatus.DEREGISTERED)) {
                logger.info("'" + email + "' already regestered! redirect to main page.");
                return new ModelAndView("redirect:/errors", MapUtil.toMap("alertMsg", (email + "\n해당 이메일은 가입되어 있습니다."),
                                                                          "errorMsg", "이미 가입된 이메일입니다."));
            }
        }

        // sign과 nonce생성
        ApiResult signNonceRst = googleOAuthSvc.genSignAndNonceForEmailValidation(email);

        if (signNonceRst == null || !signNonceRst.getResult()) {
            logger.error("'signNonceRst' is null or false!");
            return new ModelAndView("redirect:/errors", MapUtil.toMap("errorMsg", "서명값 생성에 실패했습니다."));
        }

        // 추가정보 입력페이지로 리다이렉션
        String sign = signNonceRst.getDataAsStr("sign");
        String nonce = signNonceRst.getDataAsStr("nonce");
        return new ModelAndView("redirect:/register", MapUtil.toMap("email", email, "sign", sign, "nonce", nonce));
    }
}