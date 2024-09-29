package org.entropy.feignapi.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "producer", url = "127.0.0.1:7000")
public interface InfoClient {

    @GetMapping("/info")
    public String info();

    @GetMapping("/info/list")
    public String infoList();
}
