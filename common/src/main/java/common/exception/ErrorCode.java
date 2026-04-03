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
    USER_NOT_APPROVED(HttpStatus.FORBIDDEN, "USER_NOT_APPROVED", "승인된 사용자만 로그인할 수 있습니다."),

    // ── Common ────────────────────────────────────────
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "INVALID_INPUT", "입력값이 유효하지 않습니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "접근 권한이 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다."),
    INVALID_PAGE_SIZE(HttpStatus.BAD_REQUEST, "INVALID_PAGE_SIZE", "페이지 크기는 10, 30, 50만 가능합니다."),
    INVALID_SORT_BY(HttpStatus.BAD_REQUEST, "INVALID_SORT_BY", "정렬 기준이 올바르지 않습니다."),

    // ── Company ───────────────────────────────────────
    COMPANY_NOT_FOUND(HttpStatus.NOT_FOUND, "COMPANY_NOT_FOUND", "존재하지 않는 업체입니다."),
    COMPANY_CREATE_FORBIDDEN(HttpStatus.FORBIDDEN, "COMPANY_CREATE_FORBIDDEN", "업체 생성 권한이 없습니다."),
    COMPANY_UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN, "COMPANY_UPDATE_FORBIDDEN", "업체 수정 권한이 없습니다."),
    COMPANY_HUB_MISMATCH(HttpStatus.FORBIDDEN, "COMPANY_HUB_MISMATCH", "담당 허브의 업체만 접근할 수 있습니다."),
    HUB_NOT_FOUND(HttpStatus.NOT_FOUND, "HUB_NOT_FOUND", "존재하지 않는 허브입니다."),
    COMPANY_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "COMPANY_DELETE_FORBIDDEN", "업체 삭제 권한이 없습니다."),
    COMPANY_HAS_ACTIVE_ORDERS(HttpStatus.CONFLICT, "COMPANY_HAS_ACTIVE_ORDERS", "진행 중인 주문이 있는 업체는 삭제할 수 없습니다."),
    HUB_SERVICE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "HUB_SERVICE_ERROR", "허브 서비스 호출 중 오류가 발생했습니다."),
    USER_SERVICE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "USER_SERVICE_ERROR", "유저 서비스 호출 중 오류가 발생했습니다."),
    ORDER_SERVICE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "ORDER_SERVICE_ERROR", "주문 서비스 호출 중 오류가 발생했습니다."),
    INVALID_ADDRESS(HttpStatus.BAD_REQUEST, "INVALID_ADDRESS", "유효하지 않은 주소입니다. 좌표를 조회할 수 없습니다.");

    // 각 모듈 별로 담당자가 추가

    private final HttpStatus status;
    private final String code;
    private final String message;
}
