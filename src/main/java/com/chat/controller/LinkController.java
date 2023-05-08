package com.chat.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author L
 */
@RestController
public class LinkController {

    @GetMapping("test")
    public String test(){
        return "hello";
    }
}
