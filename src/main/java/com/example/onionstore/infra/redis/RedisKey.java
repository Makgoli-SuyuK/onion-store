package com.example.onionstore.infra.redis;

public final class RedisKey {

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
}
