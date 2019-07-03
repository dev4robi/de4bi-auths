package com.authserver.controller;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import com.authserver.service.GoogleOAuthService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Controller
public class OAuthController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private GoogleOAuthService googleOAuthSvc;

    @GetMapping("/oauth/google/code2token")
    public String oauthGoogleCode(@RequestParam("code") String code, @RequestParam("state") String state) {
        String idToken = null;

        if ((idToken = googleOAuthSvc.getIdTokenFromCode(code, state)) == null) {
            logger.error("'idToken' is null!");
            return null;
        }

        String email = null;

        if ((email = googleOAuthSvc.getEmailFromIdToken(idToken)) == null) {
            logger.error("'email' is null!");
            return null;
        }

        return "redirect:/naver.com";
    }
}