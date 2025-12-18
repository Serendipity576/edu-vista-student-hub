package com.eduvista.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommonResponse<T> {
    private Integer code;
    private String message;
    private T data;
    
    public static <T> CommonResponse<T> success(T data) {
        return CommonResponse.<T>builder()
            .code(200)
            .message("操作成功")
            .data(data)
            .build();
    }
    
    public static <T> CommonResponse<T> success(String message, T data) {
        return CommonResponse.<T>builder()
            .code(200)
            .message(message)
            .data(data)
            .build();
    }
    
    public static <T> CommonResponse<T> error(String message) {
        return CommonResponse.<T>builder()
            .code(500)
            .message(message)
            .data(null)
            .build();
    }
    
    public static <T> CommonResponse<T> error(Integer code, String message) {
        return CommonResponse.<T>builder()
            .code(code)
            .message(message)
            .data(null)
            .build();
    }
}

