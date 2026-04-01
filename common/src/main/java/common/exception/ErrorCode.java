package common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ── Auth / Token ──────────────────────────────────
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "로그인이 필요합니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "EXPIRED_TOKEN", "만료된 토큰입니다. 다시 로그인해 주세요."),
    TOKEN_BLACKLISTED(HttpStatus.UNAUTHORIZED, "TOKEN_BLACKLISTED", "이미 로그아웃된 토큰입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "유효하지 않은 리프레시 토큰입니다."),
    TOKEN_MISMATCH(HttpStatus.UNAUTHORIZED, "TOKEN_MISMATCH", "토큰 정보가 일치하지 않습니다. 다시 로그인해주세요."),

    // ── User ──────────────────────────────────────────
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "존재하지 않는 사용자입니다."),
    DUPLICATE_USERNAME(HttpStatus.CONFLICT, "DUPLICATE_USERNAME", "이미 사용 중인 아이디입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "DUPLICATE_EMAIL", "이미 사용 중인 이메일입니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "INVALID_PASSWORD", "비밀번호가 일치하지 않습니다."),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "PASSWORD_MISMATCH", "비밀번호와 비밀번호 확인이 일치하지 않습니다."),
    ALREADY_DELETED(HttpStatus.CONFLICT, "ALREADY_DELETED", "이미 탈퇴한 사용자입니다."),
    WITHDRAWN_USER(HttpStatus.BAD_REQUEST, "WITHDRAWN_USER", "탈퇴한 계정입니다."),
    INVALID_ROLE(HttpStatus.BAD_REQUEST, "INVALID_ROLE", "유효하지 않은 권한 값입니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "LOGIN_FAILED", "아이디 또는 비밀번호가 올바르지 않습니다."),
    CANNOT_DELETE_SELF(HttpStatus.FORBIDDEN,"CANNOT_DELETE_SELF","본인 계정은 삭제할 수 없습니다."),
    CANNOT_UPDATE_ROLE_SELF(HttpStatus.FORBIDDEN,"CANNOT_UPDATE_ROLE_SELF","본인 권한은 변경할 수 없습니다."),

    // ── Common ────────────────────────────────────────
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "INVALID_INPUT", "입력값이 유효하지 않습니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "접근 권한이 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다."),
    INVALID_PAGE_SIZE(HttpStatus.BAD_REQUEST, "INVALID_PAGE_SIZE", "페이지 크기는 10, 30, 50만 가능합니다."),
    INVALID_SORT_BY(HttpStatus.BAD_REQUEST, "INVALID_SORT_BY", "정렬 기준이 올바르지 않습니다."),

    // 각 모듈 별로 담당자가 추가

    // ── Delivery ────────────────────────────────────────
    DELIVERY_NOT_FOUND(HttpStatus.NOT_FOUND, "DELEVERY_NOT_FOUND", "존재하지 않는 배송입니다."),
    DELIVERY_ALREADY_DELETED(HttpStatus.CONFLICT, "DELIVERY_ALREADY_DELETED", "이미 삭제된 배송입니다."),
    INVALID_DELIVERY_STATUS(HttpStatus.BAD_REQUEST, "INVALID_DELIVERY_STATUS", "유효하지 않은 배송 상태입니다."),
    INVALID_DELIVERY_SEARCH_CONDITION(HttpStatus.BAD_REQUEST, "INVALID_DELIVERY_SEARCH_CONDITION", "배송 검색 조건이 올바르지 않습니다."),

    // ── Delivery Route Record────────────────────────────
    DELIVERY_ROUTE_RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, "DELIVERY_ROUTE_RECORD_NOT_FOUND", "존재하지 않는 배송 경로 기록입니다."),
    DELIVERY_ROUTE_RECORD_ALREADY_DELETED(HttpStatus.CONFLICT, "DELIVERY_ROUTE_RECORD_ALREADY_DELETED", "이미 삭제된 배송 경로 기록입니다."),
    INVALID_DELIVERY_ROUTE_STATUS(HttpStatus.BAD_REQUEST, "INVALID_DELIVERY_ROUTE_STATUS", "유효하지 않은 배송 경로 상태입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
