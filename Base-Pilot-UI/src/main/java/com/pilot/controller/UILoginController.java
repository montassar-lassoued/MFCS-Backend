package com.pilot.controller;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.awt.*;
import java.util.List;

@Controller
public class UILoginController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/home")
    public String home(Model model, Authentication authentication) {

        String username = authentication.getName();
        var details = authentication.getDetails();
        var authorities = authentication.getAuthorities();

        List<MenuItem> menues = List.of(
                new MenuItem("Home", MenuShortcut.HOME, "#FF6B6B"),
                new MenuItem("Company", MenuShortcut.COMPANY, "#4ECDC4"),
                new MenuItem("Services", MenuShortcut.SERVICES, "#FFD93D"),
                new MenuItem("Gallery", MenuShortcut.GALLERY,"#FF6B6B"),
                new MenuItem("News", MenuShortcut.NEWS, "#4ECDC4"),
                new MenuItem("Contact", MenuShortcut.CONTACT, "#FFD93D")
        );

        model.addAttribute("menues", menues);
        return "home";
    }

    // Fallback nur für andere Pfade, z.B. /app/**, aber NICHT für /css/**:
    @GetMapping("/{path:^(?!css|js|images).*}")
    public String fallback() {
        return "error/404";
    }
}
