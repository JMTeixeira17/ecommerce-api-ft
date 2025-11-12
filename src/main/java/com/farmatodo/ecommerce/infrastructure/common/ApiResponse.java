package com.farmatodo.ecommerce.infrastructure.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private String status;
    private int code;
    private T data;
    private String error;

    public static <T> ApiResponse<T> success(T data, int code) {
        return ApiResponse.<T>builder()
                .status("success")
                .code(code)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(T data) {
        return success(data, 200);
    }

    public static <T> ApiResponse<T> error(String message, int code) {
        return ApiResponse.<T>builder()
                .status("error")
                .code(code)
                .error(message)
                .build();
    }
}