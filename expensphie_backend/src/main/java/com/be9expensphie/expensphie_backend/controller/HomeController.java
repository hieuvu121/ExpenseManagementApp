package com.be9expensphie.expensphie_backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/app-status")
public class HomeController {
    @GetMapping
    public String appCheck() {
        return "Application is running";
    }
}
