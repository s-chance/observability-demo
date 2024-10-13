package org.entropy.common.util;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;

public class Base62UrlShortener {

    private static final String BASE62_ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int BASE = BASE62_ALPHABET.length();

    /**
     * 将字符串类型的 URL 转换为 Base62 编码的短链。
     *
     * @param url 要转换的 URL 字符串。
     * @return Base62 编码的短链。
     */
    public static String encode(String url) {
        // 使用当前时间戳（毫秒）和随机数来增加唯一性
        long value = 0;

        for (byte b : url.getBytes(StandardCharsets.UTF_8)) {
            value = value * 256 + b;
        }
        value +=  ThreadLocalRandom.current().nextInt();
        StringBuilder encoded = new StringBuilder();
        while (value > 0) {
            int remainder = (int) (value % BASE);
            encoded.insert(0, BASE62_ALPHABET.charAt(remainder));
            value /= BASE;
        }
        return encoded.toString();
    }

//    public static void main(String[] args) {
//        // 测试编码
//        String originalUrl = "https://www.example.com/some/long/url/that/needs/to/be/shortened";
//        String encodedUrl = null;
//        do {
//            encodedUrl = encode(originalUrl);
//            System.out.println("Encoded URL: " + encodedUrl);
//        } while (encodedUrl.length() != 0);
//    }
}
