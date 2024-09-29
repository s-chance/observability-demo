package org.entropy.consumer.controller;

import lombok.extern.slf4j.Slf4j;
import org.entropy.feignapi.api.InfoClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class GetInfoController {


    private final InfoClient infoClient;

    public GetInfoController(InfoClient infoClient) {
        this.infoClient = infoClient;
    }

    @GetMapping("/test")
    public String test() {
        log.info("consumer test");
        return infoClient.info();
    }

    @GetMapping("/test/list")
    public String testList() {
        log.info("consumer list");
        return infoClient.infoList();
    }

    @GetMapping("/test/local")
    public String testLocal() {
        log.info("consumer local");
        return "local";
    }
}
