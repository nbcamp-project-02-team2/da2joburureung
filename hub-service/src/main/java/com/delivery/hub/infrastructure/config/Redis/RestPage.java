package com.delivery.hub.infrastructure.config.Redis;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import java.io.Serializable;
import java.util.List;

@Getter
@NoArgsConstructor // 역직렬화에 필수
public class RestPage<T> implements Serializable {

    private List<T> content;
    private int number;
    private int size;
    private long totalElements;

    public RestPage(Page<T> page) {
        this.content = page.getContent();
        this.number = page.getNumber();
        this.size = page.getSize();
        this.totalElements = page.getTotalElements();
    }

    public Page<T> toPage() {
        return new PageImpl<>(content, PageRequest.of(number, size), totalElements);
    }
}