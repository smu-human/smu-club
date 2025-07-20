package com.smuclub.smu_club.club.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/club")
public class ClubController {

    @GetMapping("/")
    public String club(){
        return "club";
    }

/*    @GetMapping("/app-form")
    public String showApplicationForm(Model model){
        //DB에서 FormDTO필요
    }*/

/*    @PostMapping("/app-form/sumbit")
    public String submitApplication(@ModelAttribute ){
        //지원서 처리로직
    }*/
}
