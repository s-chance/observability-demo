package org.entropy.feignapi.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "producer", path = "/info")
@Deprecated
public interface InfoClient {

    @GetMapping
    public String info();

    @GetMapping("/list")
    public String infoList();
}
