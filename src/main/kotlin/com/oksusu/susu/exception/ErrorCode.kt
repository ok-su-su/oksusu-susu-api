package com.oksusu.susu.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(val status: HttpStatus, val description: String) {
    /** Common Error Code */
    BAD_REQUEST_ERROR(HttpStatus.BAD_REQUEST, "bad request"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류, 관리자에게 문의하세요"),
    INVALID_INPUT_VALUE_ERROR(HttpStatus.BAD_REQUEST, "input is invalid value"),
    INVALID_TYPE_VALUE_ERROR(HttpStatus.BAD_REQUEST, "invalid type value"),
    METHOD_NOT_ALLOWED_ERROR(HttpStatus.METHOD_NOT_ALLOWED, "Method type is invalid"),
    INVALID_MEDIA_TYPE_ERROR(HttpStatus.BAD_REQUEST, "invalid media type"),
    QUERY_DSL_NOT_EXISTS_ERROR(HttpStatus.NOT_FOUND, "not found query dsl"),
    COROUTINE_CANCELLATION_ERROR(HttpStatus.BAD_REQUEST, "coroutine cancellation error"),
    NO_AUTHORITY_ERROR(HttpStatus.FORBIDDEN, "수정 권한이 없습니다."),
    FAIL_TO_TRANSACTION_TEMPLATE_EXECUTE_ERROR(HttpStatus.BAD_REQUEST, "fail to tx-templates execute error"),
    FAIL_TO_REDIS_EXECUTE_ERROR(HttpStatus.BAD_REQUEST, "fail to redis execute error"),

    /** Auth Error Code */
    FAIL_TO_VERIFY_TOKEN_ERROR(HttpStatus.UNAUTHORIZED, "fail to verify token"),
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "유효한 엑세스 토큰이 아닙니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "유효한 리프레시 토큰이 아닙니다."),
    INVALID_TOKEN(HttpStatus.BAD_REQUEST, "유효한 토큰이 아닙니다."),

    /** User Error Code */
    NOT_FOUND_USER_ERROR(HttpStatus.NOT_FOUND, "유저 정보를 찾을 수 없습니다."),
    ALREADY_REGISTERED_USER(HttpStatus.NOT_FOUND, "이미 가입된 유저입니다."),

    /** User Device Error Code */
    NOT_FOUND_USER_DEVICE_ERROR(HttpStatus.NOT_FOUND, "유저 디바이스 정보를 찾을 수 없습니다."),

    /** Ledger Error Code */
    LEDGER_INVALID_DUE_DATE_ERROR(HttpStatus.BAD_REQUEST, "잘못된 일정 등록 요청입니다."),
    NOT_FOUND_LEDGER_ERROR(HttpStatus.NOT_FOUND, "장부 정보가 없습니다."),

    /** Category Error Code */
    NOT_FOUND_CATEGORY_ERROR(HttpStatus.NOT_FOUND, "카테고리 정보를 찾을 수 없습니다."),

    /** Category Assignment Error Code */
    NOT_FOUND_CATEGORY_ASSIGNMENT_ERROR_CODE(HttpStatus.NOT_FOUND, "카테고리 매핑 정보를 찾을 수 없습니다."),

    /** Relationship Error Code */
    NOT_FOUND_RELATIONSHIP_ERROR(HttpStatus.NOT_FOUND, "관계 정보를 찾을 수 없습니다."),

    /** Friend Error Code */
    NOT_FOUND_FRIEND_ERROR(HttpStatus.NOT_FOUND, "친구 정보를 찾을 수 없습니다."),
    ALREADY_REGISTERED_FRIEND_PHONE_NUMBER_ERROR(HttpStatus.BAD_REQUEST, "이미 등록된 전화번호 입니다."),

    /** Friend Relationship Error Code */
    NOT_FOUND_FRIEND_RELATIONSHIP_ERROR(HttpStatus.NOT_FOUND, "친구 관계 정보를 찾을 수 없습니다."),

    /** Envelope Error Code */
    NOT_FOUND_ENVELOPE_ERROR(HttpStatus.NOT_FOUND, "봉투 정보를 찾을 수 없습니다."),

    /** Post Error Code */
    NOT_FOUND_POST_ERROR(HttpStatus.NOT_FOUND, "게시글 정보를 찾을 수 없습니다."),
    INVALID_VOTE_OPTION_SEQUENCE(HttpStatus.BAD_REQUEST, "투표 옵션 순서가 잘못되었습니다."),
    NOT_FOUND_VOTE_ERROR(HttpStatus.NOT_FOUND, "투표 정보를 찾을 수 없습니다."),
    DUPLICATED_VOTE_ERROR(HttpStatus.NOT_FOUND, "중복 투표를 할 수 없습니다."),

    /** Board Error Code */
    NOT_FOUND_BOARD_ERROR(HttpStatus.NOT_FOUND, "보드 정보를 찾을 수 없습니다."),

    /** Term Error Code */
    NOT_FOUND_TERM_ERROR(HttpStatus.NOT_FOUND, "약관 정보를 찾을 수 없습니다."),

    /** Vote Option Summary Error Code */
    NOT_FOUND_VOTE_OPTION_SUMMARY_ERROR(HttpStatus.BAD_REQUEST, "투표 옵션 요약 정보를 찾을 수 없습니다."),

    /** Vote Summary Error Code */
    NOT_FOUND_VOTE_SUMMARY_ERROR(HttpStatus.BAD_REQUEST, "투표 요약 정보를 찾을 수 없습니다."),

    /** Vote History Error Code */
    ALREADY_VOTED_POST(HttpStatus.BAD_REQUEST, "이미 진행된 투표입니다."),

    /** Block Error Code */
    ALREADY_BLOCKED_TARGET(HttpStatus.BAD_REQUEST, "이미 차단한 대상입니다."),
    NOT_BLOCKED_TARGET(HttpStatus.BAD_REQUEST, "차단하지 않은 대상입니다."),
    CANNOT_BLOCK_MYSELF(HttpStatus.BAD_REQUEST, "본인을 차단할 수 없습니다."),
    NOT_FOUND_BLOCK_ERROR(HttpStatus.NOT_FOUND, "차단 정보를 찾을 수 없습니다."),

    /** Report Error Code */
    NOT_FOUND_REPORT_METADATA_ERROR(HttpStatus.NOT_FOUND, "신고 메타데이터 정보를 찾을 수 없습니다."),
    ALREADY_EXISTS_REPORT_HISTORY_ERROR(HttpStatus.BAD_REQUEST, "이미 신고한 상태입니다."),

    /** Statistic Error Code */
    NOT_FOUND_SUSU_BASIC_STATISTIC_ERROR(HttpStatus.NOT_FOUND, "수수 통계 자료를 찾을 수 없습니다."),

    /** Count Error Code */
    NOT_FOUND_COUNT_ERROR(HttpStatus.NOT_FOUND, "카운트 정보를 찾을 수 없습니다."),
    ;
}
