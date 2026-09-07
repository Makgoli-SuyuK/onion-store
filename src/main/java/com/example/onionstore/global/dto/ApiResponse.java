package com.example.onionstore.global.dto;

import com.example.onionstore.global.exception.ErrorCode;
import java.util.Map;

public record ApiResponse<T>(boolean success, String code, String message, T data) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "SUCCESS", null, data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, "SUCCESS", message, data);
    }

    public static ApiResponse<Void> fail(ErrorCode errorCode) {
        return new ApiResponse<>(false, errorCode.getCode(), errorCode.getMessage(), null);
    }

    public static ApiResponse<Map<String, String>> validationFail(Map<String, String> fieldErrors) {
        return new ApiResponse<>(false, ErrorCode.INVALID_INPUT_VALUE.getCode(),
                ErrorCode.INVALID_INPUT_VALUE.getMessage(), fieldErrors);
    }
}
