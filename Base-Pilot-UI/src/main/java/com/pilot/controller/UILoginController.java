package com.pilot.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class UILoginController {

    @GetMapping()
    private void login(){
        System.out.println("Login");
    }
}
