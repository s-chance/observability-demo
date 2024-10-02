package org.entropy.producer.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/info")
@Slf4j
public class InfoController {

    @GetMapping
    public String info() {
        log.info("producer info");
        return "Hello World!";
    }

    @GetMapping("/list")
    public String list() {
        log.info("producer list");
        return "list!";
    }

    @GetMapping("/local")
    public String local() {
        log.info("producer local");
        return "local!";
    }

    @GetMapping("/okhttp")
    public String okhttp() {
        log.info("producer okhttp");
        return "okhttp!";
    }
}
