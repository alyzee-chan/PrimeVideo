package com.primevideo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StaticPagesController {

    @GetMapping("/about")
    public String about() {
        return "static/about";
    }

    @GetMapping("/help")
    public String help() {
        return "static/help";
    }

    @GetMapping("/legal")
    public String legal() {
        return "static/legal";
    }
}
