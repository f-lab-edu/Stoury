package com.stoury.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {
    @RequestMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE) // NOSONAR
    public Resource lobby() {
        return new ClassPathResource("static/docs/index.html");
    }
}
