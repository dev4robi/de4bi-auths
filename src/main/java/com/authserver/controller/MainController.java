package com.authserver.controller;

import java.util.HashMap;
import java.util.Map;

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
        HttpUtil.httpGet("https://naver.com", null, null, null);
        obj.put("googleLoginUrl", "https://naver.com");
        return new ModelAndView("main", obj);
    }

    @RequestMapping("/register")
    public ModelAndView registerGoogle() {
        return new ModelAndView("register");
    }
}