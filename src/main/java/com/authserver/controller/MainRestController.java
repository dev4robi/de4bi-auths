package com.authserver.controller;

import com.robi.util.HttpUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class MainRestController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @GetMapping("/test")
    public ModelAndView test() {
        return new ModelAndView("google");
    }
}