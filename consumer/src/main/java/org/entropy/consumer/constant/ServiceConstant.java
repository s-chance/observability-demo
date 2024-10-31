package org.entropy.consumer.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ServiceConstant {

    public static final String PREFIX = "http://";

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Producer {
        public static final String NAME = "producer";
        public static final String SERVICE = PREFIX + NAME;
    }
}
