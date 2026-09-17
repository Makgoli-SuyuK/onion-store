package com.example.onionstore.infra.redis;

import java.time.Duration;

public final class RedisProperty {

    public static String productInfo(Long productId) {
        return "product:" + productId;
    }

    public static String productStock(Long productId) {
        return "product_stock:" + productId;
    }

    public static String productLike(Long productId) {
        return "product_like:" + productId;
    }

    public static final String PRODUCT_POPULAR = "product:popular";

    public static final String CATEGORY = "categories";

    public static final Duration PRODUCT_INFO_DURATION = Duration.ofSeconds(60);
    public static final Duration PRODUCT_STOCK_DURATION = Duration.ofSeconds(5);
    public static final Duration PRODUCT_LIKE_DURATION = Duration.ofSeconds(10);
    public static final Duration PRODUCT_POPULAR_DURATION = Duration.ofSeconds(10);
    public static final Duration CATEGORY_DURATION = Duration.ofSeconds(180);
}
