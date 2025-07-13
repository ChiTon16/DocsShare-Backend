package com.tonz.tonzdocs.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Test {

    @GetMapping("/")
    public String home() {
        return "Welcome to TonzDocs!";
    }
}
