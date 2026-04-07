package com.da2jobu.deliveryservice.presentation.interceptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 내부 시스템 전용 엔드포인트 표시용 어노테이션.
 * Gateway를 통과한 요청(X-User-Role 헤더 존재)은 접근 거부된다.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InternalOnly {
}