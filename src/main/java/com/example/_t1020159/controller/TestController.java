package com.example._t1020159.controller;

import ch.qos.logback.core.model.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class TestController {
    @GetMapping("/todolist")
    public String index() {
        return "items"; // Tên file trong /templates/
    }
}
