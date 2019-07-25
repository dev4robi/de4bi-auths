package com.authserver.controller;

import com.authserver.data.ApiResult;
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
            return new ModelAndView("redirect:/error");
        }

        // idToken으로부터 email획득
        String email = null;
        ApiResult emailApi = googleOAuthSvc.getEmailFromIdToken(idToken);

        if (emailApi != null && emailApi.getResult()) {
            email = emailApi.getDataAsStr("email");
        }

        if (email == null) {
            logger.error("'email' is null!");
            return new ModelAndView("redirect:/error");
        }

        // 기존 가입여부 확인
        ApiResult existedUser = usersSvc.selectUserByKey("email", email);
        
        if (!existedUser.getResult()) { // 신규회원 가입
            // sign과 nonce생성
            ApiResult signNonceRst = googleOAuthSvc.genSignAndNonceForEmailValidation(email);

            if (signNonceRst == null || !signNonceRst.getResult()) {
                logger.error("'signNonceRst' is null or false!");
                return new ModelAndView("redirect:/error");
            }

            // 추가정보 입력페이지로 리다이렉션
            String sign = signNonceRst.getDataAsStr("sign");
            String nonce = signNonceRst.getDataAsStr("nonce");
            return new ModelAndView("redirect:/register", MapUtil.toMap("email", email, "sign", sign, "nonce", nonce));
        }
        
        return new ModelAndView("redirect:/main", MapUtil.toMap("alertMsg", (email + "\n해당 이메일은 이미 가입되었습니다!")));
        // @@ 여기부터 시작, 오류메시지를 전달할수 있는 방법은?
    }
}