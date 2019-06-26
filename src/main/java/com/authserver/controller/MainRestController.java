package com.authserver.controller;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import com.robi.util.HttpUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class MainRestController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @GetMapping("/test")
    public ModelAndView test() {
        return new ModelAndView("google");
    }

    @PostMapping("/test")
    public String testPost(HttpServletRequest request) {
        logger.info("<Headers>");
        Enumeration<String> it = request.getHeaderNames();
        while (it.hasMoreElements()) {
            String headrName = it.nextElement();
            logger.info(headrName + " : " + request.getHeader(headrName));
        }
        
        logger.info("<Bodies>");
        it = request.getParameterNames();
        while (it.hasMoreElements()) {
            String bodyName = it.nextElement();
            logger.info(bodyName + " : " + request.getParameter(bodyName));
        }
        
        return "hello post!";
    }
}