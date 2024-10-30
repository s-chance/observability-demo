package org.entropy.feignapi.api;

import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("/info")
public interface InfoExchangeClient {

    @GetExchange
    String info();

    @GetExchange("/list")
    String infoList();
}
