package org.entropy.feignapi.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "producer", url = "127.0.0.1:7000", path = "/info")
public interface InfoClient {

    @GetMapping
    public String info();

    @GetMapping("/list")
    public String infoList();
}
