package com.example.onionstore.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 공통
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_001", "요청 형식이 올바르지 않습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON_002", "입력값을 확인해주세요."),
    INVALID_REQUEST_BODY(HttpStatus.BAD_REQUEST, "COMMON_003", "요청 본문 형식이 올바르지 않습니다."),
    MISSING_REQUEST_VALUE(HttpStatus.BAD_REQUEST, "COMMON_004", "필수 요청값이 누락되었습니다."),
    URL_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_005", "요청한 주소를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON_006", "지원하지 않는 HTTP 메서드입니다."),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "COMMON_007", "지원하지 않는 콘텐츠 형식입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_008", "서버 오류가 발생했습니다."),

    // 인증·인가
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_001", "인증이 필요합니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_002", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_003", "만료된 토큰입니다."),
    FORBIDDEN_ROLE(HttpStatus.FORBIDDEN, "AUTH_004", "해당 작업을 수행할 권한이 없습니다."),

    // 회원
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER_001", "이미 사용 중인 이메일입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_002", "회원을 찾을 수 없습니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "USER_003", "이메일 또는 비밀번호가 올바르지 않습니다."),

    // 카테고리
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CATEGORY_001", "카테고리를 찾을 수 없습니다."),
    DUPLICATE_CATEGORY(HttpStatus.CONFLICT, "CATEGORY_002", "이미 존재하는 카테고리입니다."),

    // 상품
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT_001", "상품을 찾을 수 없습니다."),
    PRODUCT_NOT_SELLING(HttpStatus.CONFLICT, "PRODUCT_002", "현재 판매할 수 없는 상품입니다."),
    INVALID_PRICE(HttpStatus.BAD_REQUEST, "PRODUCT_003", "가격은 0 이상이어야 합니다."),
    INVALID_STOCK(HttpStatus.BAD_REQUEST, "PRODUCT_004", "재고는 0 이상이어야 합니다."),
    PRODUCT_OUT_OF_STOCK(HttpStatus.CONFLICT, "PRODUCT_005", "상품 재고가 부족합니다."),

    // 상품 찜
    ALREADY_LIKED_PRODUCT(HttpStatus.CONFLICT, "LIKE_001", "이미 찜한 상품입니다."),
    PRODUCT_LIKE_NOT_FOUND(HttpStatus.NOT_FOUND, "LIKE_002", "찜 내역을 찾을 수 없습니다."),

    // 장바구니
    CART_NOT_FOUND(HttpStatus.NOT_FOUND, "CART_001", "장바구니를 찾을 수 없습니다."),
    CART_EMPTY(HttpStatus.BAD_REQUEST, "CART_002", "장바구니가 비어있습니다."),
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "CART_003", "장바구니 상품을 찾을 수 없습니다."),
    INVALID_CART_ITEM_QUANTITY(HttpStatus.BAD_REQUEST, "CART_004", "수량은 1개 이상이어야 합니다."),

    // 주문
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER_001", "주문을 찾을 수 없습니다."),
    ORDER_NOT_PAYABLE(HttpStatus.CONFLICT, "ORDER_002", "현재 결제할 수 없는 주문입니다."),
    INVALID_ORDER_STATUS(HttpStatus.BAD_REQUEST, "ORDER_003", "유효하지 않은 주문 상태 변경입니다."),
    ORDER_ALREADY_CANCELED(HttpStatus.CONFLICT, "ORDER_004", "이미 취소된 주문입니다."),
    CANNOT_CANCEL_ORDER(HttpStatus.BAD_REQUEST, "ORDER_005", "현재 상태에서는 주문을 취소할 수 없습니다."),

    // 결제
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PAYMENT_001", "결제 정보를 찾을 수 없습니다."),
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "PAYMENT_002", "결제 금액이 주문 금액과 일치하지 않습니다."),
    INVALID_PAYMENT_STATUS(HttpStatus.BAD_REQUEST, "PAYMENT_003", "유효하지 않은 결제 상태 변경입니다."),
    PAYMENT_ALREADY_PROCESSED(HttpStatus.CONFLICT, "PAYMENT_004", "이미 처리된 결제입니다."),
    PAYMENT_FAILED(HttpStatus.BAD_REQUEST, "PAYMENT_005", "결제 처리에 실패했습니다."),

    // 이벤트
    EVENT_NOT_STARTED(HttpStatus.CONFLICT, "EVENT_001", "아직 타임세일 시작 전입니다."),
    EVENT_ENDED(HttpStatus.CONFLICT, "EVENT_002", "타임세일이 종료되었습니다."),
    LOCK_ACQUISITION_FAILED(HttpStatus.CONFLICT, "EVENT_003", "요청이 몰리고 있습니다. 잠시 후 다시 시도해주세요."),

    // 채팅
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_001", "채팅방을 찾을 수 없습니다."),
    CHAT_ROOM_ACCESS_DENIED(HttpStatus.FORBIDDEN, "CHAT_002", "해당 채팅방에 참여하지 않은 사용자입니다."),
    CHAT_MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_003", "채팅 메시지를 찾을 수 없습니다."),
    CHAT_MESSAGE_EMPTY(HttpStatus.BAD_REQUEST, "CHAT_004", "메시지 내용이 비어있습니다."),
    CHAT_ROOM_CREATE_FORBIDDEN_FOR_ADMIN(HttpStatus.FORBIDDEN, "CHAT_005", "관리자는 문의방을 생성할 수 없습니다."),
    CHAT_ROOM_STATUS_CHANGE_ADMIN_ONLY(HttpStatus.FORBIDDEN,"CHAT_006", "관리자만 상태를 변경할 수 있습니다."),
    INVALID_CHAT_ROOM_STATUS_TRANSITION(HttpStatus.CONFLICT,"CHAT_007","완료된 문의는 상태를 되돌릴 수 없습니다."),
    INVALID_CHAT_MESSAGE_LENGTH(HttpStatus.BAD_REQUEST,"CHAT_008","메시지는 1~1000자여야 합니다."),
    CANNOT_SEND_TO_COMPLETED_CHAT_ROOM(HttpStatus.CONFLICT,"CHAT_009","완료된 문의에는 메시지를 보낼 수 없습니다.");


    private final HttpStatus status;
    private final String code;
    private final String message;
}
