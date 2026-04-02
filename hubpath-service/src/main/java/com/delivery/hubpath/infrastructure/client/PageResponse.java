package com.delivery.hubpath.infrastructure.client;

import lombok.*;
import org.springframework.data.domain.Page;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PageResponse<T> {

    private List<T> content;          // 실제 데이터 리스트 (HubResponse 등)
    private int pageNumber;           // 현재 페이지 번호
    private int pageSize;             // 한 페이지당 데이터 개수
    private long totalElements;       // 전체 데이터 개수
    private int totalPages;           // 전체 페이지 수
    private boolean last;             // 마지막 페이지 여부

    public static <T> PageResponse<T> of(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
