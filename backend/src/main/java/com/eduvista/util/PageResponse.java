package com.eduvista.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResponse<T> implements Serializable {
    private List<T> content;          // 数据列表
    private long totalElements;       // 总记录数
    private int totalPages;           // 总页数
    private int size;                 // 每页大小
    private int number;               // 当前页码

    // 静态转换方法：方便从 Spring 的 Page 对象转换过来
    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(
            page.getContent(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.getSize(),
            page.getNumber()
        );
    }
}