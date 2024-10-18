package org.entropy.common.util;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;

public class Base62UrlShortener {

    private static final String BASE62_ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int BASE = BASE62_ALPHABET.length();
    private static final HashFunction hashFunction = Hashing.murmur3_32_fixed();

    /**
     * 将字符串类型的 URL 转换为 Base62 编码的短链。
     *
     * @param url 要转换的 URL 字符串。
     * @return Base62 编码的短链。
     */
    public static String encode(String url) {
        return encode(url, 6);
    }

    /**
     * 将字符串类型的 URL 转换为 Base62 编码的短链。
     *
     * @param url       要转换的 URL 字符串。
     * @param maxLength 最大长度限制。
     * @return Base62 编码的短链。
     */
    public static String encode(String url, int maxLength) {
        // 使用MurmurHash32算法生成哈希值
        int hashValues = hashFunction.hashString(url, StandardCharsets.UTF_8).asInt();

        StringBuilder encoded = new StringBuilder();

        // 使用32位哈希值生成Base62编码
        while (encoded.length() < maxLength) {
            // 确保余数为非负数
            int remainder = (hashValues % BASE + BASE) % BASE;
            encoded.insert(0, BASE62_ALPHABET.charAt(remainder));
            hashValues /= BASE;
        }
        return encoded.toString();
    }
}
