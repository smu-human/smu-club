package com.smuclub.smu_club.index.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j
public class IndexController {

    @RequestMapping("/")
    public String index(){
        log.info("index Controller");
        return "index";
    }

}
