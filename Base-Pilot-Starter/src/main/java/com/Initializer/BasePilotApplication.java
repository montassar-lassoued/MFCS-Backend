package com.Initializer;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class BasePilotApplication {
     static void main(String[] args) {
        SpringApplication a = new SpringApplication(BasePilotApplication.class);
        a.setBannerMode(Banner.Mode.OFF);
        a.run(args);
    }
}