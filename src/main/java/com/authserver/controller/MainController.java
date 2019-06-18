package com.authserver.controller;

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
        
        return new ModelAndView("main");
    }

    @RequestMapping("/register")
    public ModelAndView registerGoogle() {
        return new ModelAndView("register");
    }
}