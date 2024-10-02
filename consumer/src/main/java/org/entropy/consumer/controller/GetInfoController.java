package org.entropy.consumer.controller;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.entropy.feignapi.api.InfoClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

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

    private final OkHttpClient client = new OkHttpClient();

    @GetMapping("/test/okhttp")
    public String testOkHttp() {
        log.info("consumer okhttp");
        try (Response response = client.newCall(new Request.Builder()
                .url("http://localhost:7000/info/okhttp")
                .build()).execute()) {
            return response.body() != null ? response.body().string() : null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
