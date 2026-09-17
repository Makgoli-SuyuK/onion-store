# 프로젝트 전체 API 명세

> 현재 `dev` 브랜치의 Controller·Security 설정 기준 통합 명세다. 팀 README와 프론트엔드 연동은 이 문서를 기준으로 한다. 기존 `결제 API 명세.md`, `환불 API 명세.md`, `웹훅·결제·환불 API 명세서 (현재 코드 기준).md`는 설계 논의와 세부 정책을 남긴 문서이며, 경로·응답 계약은 이 문서와 실제 Swagger를 우선한다.

## 공통 규칙

- 기본 주소: `/api`
- 인증이 필요한 요청은 `Authorization: Bearer {accessToken}` 헤더를 사용한다.
- 성공·실패 응답은 모두 `ApiResponse<T>` 형식이다.
- `Pageable`을 사용하는 목록 API는 `page`, `size`, `sort` 쿼리 파라미터를 사용할 수 있다. 페이지 번호는 Spring Data 기준으로 `0`부터 시작한다. 단, 관리자 환불 목록만 `page`를 `1`부터 받는다.
- Swagger UI: `/swagger-ui/index.html`

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "성공 메시지 또는 null",
  "data": {}
}
```

인증·인가 실패는 `401 AUTH_001`, `403 AUTH_004` 형식으로 응답한다. 입력값 검증 실패는 `400 COMMON_002`와 필드별 오류를 반환한다.

## API 목록

### 인증·회원

| 메서드 | 경로 | 권한 | 요청 본문 | 성공 응답 `data` | 설명 |
| --- | --- | --- | --- | --- | --- |
| POST | `/api/auth/signup` | 공개 | `SignupRequest` | `SignupResponse` | 회원가입 |
| POST | `/api/auth/login` | 공개 | `LoginRequest` | `LoginResponse` | 로그인 및 Access Token 발급 |
| GET | `/api/users/me` | 로그인 | 없음 | `UserMeResponse` | 내 정보 조회 |
| PATCH | `/api/users/me` | 로그인 | `UserProfileUpdateRequest` | `UserMeResponse` | 이름·전화번호 수정 |
| PATCH | `/api/users/me/password` | 로그인 | `PasswordChangeRequest` | `null` | 비밀번호 변경 |
| DELETE | `/api/users/me` | 로그인 | `WithdrawRequest` | `null` | 회원 탈퇴 |

### 카테고리·상품

| 메서드 | 경로 | 권한 | 요청·쿼리 | 성공 응답 `data` | 설명 |
| --- | --- | --- | --- | --- | --- |
| GET | `/api/categories` | 공개 | 없음 | `List<Category>` | 전체 카테고리 조회 |
| POST | `/api/categories` | 관리자 | `CategoryCreateRequest` | `null` | 카테고리 생성 |
| PATCH | `/api/categories/{categoryId}` | 관리자 | `CategoryEditRequest` | `null` | 카테고리 이름 수정 |
| DELETE | `/api/categories/{categoryId}` | 관리자 | 없음 | `null` | 카테고리 삭제 |
| GET | `/api/products` | 공개 | `category`, `name`, `priceStart`, `priceEnd`, `likeCount`, `page`, `size`, `sort` | `Page<ProductSimpleResponse>` | 상품 검색·목록 조회 |
| GET | `/api/products/{productId}` | 공개 | 없음 | `ProductDetailWithLiked` | 상품 상세 조회. 로그인 상태면 좋아요 여부도 포함 |
| POST | `/api/products` | 관리자 | `ProductCreateRequest` | `null` | 상품 등록 |
| PATCH | `/api/products/{productId}` | 관리자 | `ProductEditRequest` | `ProductResponse` | 상품 정보·판매 상태 수정 |
| DELETE | `/api/products/{productId}` | 관리자 | 없음 | `null` | 상품 삭제 |
| POST | `/api/products/{productId}/like` | 로그인 | 없음 | `null` | 좋아요 추가 또는 취소 |
| GET | `/api/products/like-count-top-10` | 공개 | 없음 | `List<ProductSimpleResponse>` | 좋아요 수 상위 10개 상품 조회 |

### 장바구니·주문

| 메서드 | 경로 | 권한 | 요청·쿼리 | 성공 응답 `data` | 설명 |
| --- | --- | --- | --- | --- | --- |
| GET | `/api/carts` | 로그인 | 없음 | `CartResponse` | 내 장바구니 조회 |
| POST | `/api/carts/items` | 로그인 | `AddCartItemRequest` | `CartItemResponse` | 장바구니에 상품 추가. `201 Created` |
| PATCH | `/api/carts/items/{cartItemId}` | 로그인 | `UpdateCartItemQuantityRequest` | `CartItemResponse` | 장바구니 항목 수량 변경 |
| DELETE | `/api/carts/items/{cartItemId}` | 로그인 | 없음 | `null` | 장바구니 항목 삭제 |
| GET | `/api/orders` | 로그인 | `keyword`, `startDate`, `endDate`, `customerId`, `page`, `size`, `sort` | `Page<GetOrderListResponse>` | 고객은 내 주문, 관리자는 조건에 맞는 주문 조회 |
| GET | `/api/orders/{orderId}` | 로그인 | 없음 | `GetOrderResponse` | 주문 상세 조회 |
| POST | `/api/orders` | 로그인 | 선택: `OrderCreateRequest` | `OrderCreateResponse` | 주문 생성. 본문이 없거나 `cartItemIds`가 비어 있으면 장바구니 전체 주문 |
| POST | `/api/orders/{orderId}/cancel` | 로그인 | 없음 | `CancelOrderResponse` | 결제 전 주문 취소 |
| PATCH | `/api/orders/admin/{orderId}` | 관리자 | `ChangeOrderStatusRequest` | `ChangeOrderStatusResponse` | 주문 상태 변경 |

### 결제·PortOne 웹훅

| 메서드 | 경로 | 권한 | 요청·헤더 | 성공 응답 `data` | 설명 |
| --- | --- | --- | --- | --- | --- |
| GET | `/api/config/portone` | 공개 | 없음 | `PortOneConfigResponse` | 프론트 결제창용 `storeId`, `channelKey` 조회. API Secret은 반환하지 않음 |
| POST | `/api/payments/confirm` | 로그인 | `PaymentConfirmRequest` | `PaymentConfirmResponse` | PortOne 결제 상태·금액을 서버가 재조회한 뒤 주문·결제 상태 확정 |
| POST | `/api/webhooks/portone` | PortOne | `webhook-id`, `webhook-timestamp`, `webhook-signature`, 원문 body | `null` | PortOne 웹훅 서명 검증 및 결제 상태 동기화 |

웹훅은 `webhook-id`를 중복 없이 저장한다. 같은 이벤트가 재전송돼도 완료된 이벤트는 다시 처리하지 않으며, 웹훅 본문만 신뢰하지 않고 PortOne 결제 조회 결과를 다시 검증한다.

### 환불

| 메서드 | 경로 | 권한 | 요청·쿼리 | 성공 응답 `data` | 설명 |
| --- | --- | --- | --- | --- | --- |
| POST | `/api/orders/{orderId}/refunds` | 고객 | `CustomerRefundRequest` | `CustomerRefundSummaryResponse` | 주문 항목 전체 또는 일부 수량 환불 요청. `201 Created` |
| GET | `/api/refunds` | 고객 | 없음 | `List<CustomerRefundSummaryResponse>` | 내 환불 이력 조회 |
| GET | `/api/refunds/{refundId}` | 고객 | 없음 | `CustomerRefundDetailResponse` | 내 환불 상세 조회 |
| GET | `/api/admin/refunds` | 관리자 | `status`, `from`, `to`, `keyword`, `page`, `size` | `AdminRefundPageResponse` | 환불 요청·처리 이력 검색 |
| GET | `/api/admin/refunds/{refundId}` | 관리자 | 없음 | `AdminRefundDetailResponse` | 환불 상세 조회 |
| POST | `/api/admin/refunds/{refundId}/approve` | 관리자 | 없음 | `RefundReviewResponse` | 환불 승인 및 PortOne 취소 요청. 결과에 따라 `200 OK` 또는 `202 Accepted` |
| POST | `/api/admin/refunds/{refundId}/reject` | 관리자 | `RefundRejectRequest` | `RefundReviewResponse` | 환불 요청 거절 |

환불 상태는 `PENDING_APPROVAL`, `REQUESTED`, `COMPLETED`, `REJECTED`, `FAILED`를 사용한다. 고객 요청은 `PENDING_APPROVAL`으로 저장되며, 승인 뒤 PortOne 취소 API 응답에 따라 최종 상태가 반영된다.

### 채팅 REST API

| 메서드 | 경로 | 권한 | 요청·쿼리 | 성공 응답 `data` | 설명 |
| --- | --- | --- | --- | --- | --- |
| POST | `/api/chats/rooms` | 고객 | `ChatRoomCreateRequest` | `ChatRoomCreateResponse` | 고객 문의방 생성. `201 Created` |
| GET | `/api/chats/rooms` | 로그인 | `status`, `page`, `size`, `sort` | `Page<ChatRoomListResponse>` | 고객은 내 문의방, 관리자는 전체 문의방 조회 |
| GET | `/api/chats/rooms/{roomId}` | 로그인 | 없음 | `ChatRoomDetailResponse` | 문의방 상세 조회 |
| GET | `/api/chats/rooms/{roomId}/messages` | 로그인 | `lastMessageId`, `size` | `ChatMessageListResponse` | 메시지 이력 커서 조회. `size` 기본값은 30 |
| PATCH | `/api/admin/chats/rooms/{roomId}/status` | 관리자 | `ChatRoomStatusUpdateRequest` | `ChatRoomStatusUpdateResponse` | 문의 상태 변경 |

### STOMP WebSocket 채팅

| 구분 | 주소·Destination | 인증 | 설명 |
| --- | --- | --- | --- |
| 연결 | `/ws-stomp` | CONNECT 헤더에 `Authorization: Bearer {accessToken}` | STOMP 연결. JWT를 검증해 사용자 Principal을 설정 |
| 구독 | `/sub/chats/rooms/{roomId}` | 로그인·채팅방 접근 권한 | 해당 문의방의 실시간 메시지를 구독 |
| 전송 | `/pub/chats/rooms/{roomId}/messages` | 연결된 사용자 | 본문 `ChatMessageSendRequest`를 전송. 서버가 저장한 뒤 구독자에게 방송 |

## 요청 본문 형식

### 인증·회원

```json
// SignupRequest
{ "email": "user@example.com", "password": "비밀번호", "name": "홍길동", "phoneNumber": "010-1234-5678" }

// LoginRequest
{ "email": "user@example.com", "password": "비밀번호" }

// UserProfileUpdateRequest
{ "name": "홍길동", "phoneNumber": "010-1234-5678" }

// PasswordChangeRequest 또는 WithdrawRequest
{ "currentPassword": "현재 비밀번호", "newPassword": "새 비밀번호" }
// WithdrawRequest는 { "password": "현재 비밀번호" }
```

### 카테고리·상품·장바구니

```json
// CategoryCreateRequest
{ "name": "깐양파" }

// CategoryEditRequest
{ "newName": "햇양파" }

// ProductCreateRequest
{ "category": "햇양파", "productName": "국내산 햇양파 5kg", "description": "설명", "price": 15000, "stock": 100 }

// ProductEditRequest
{ "name": "국내산 햇양파 5kg", "description": "설명", "price": 15000, "stock": 100, "status": "SELLING" }

// AddCartItemRequest 또는 UpdateCartItemQuantityRequest
{ "productId": 1, "quantity": 2 }
// UpdateCartItemQuantityRequest는 { "quantity": 2 }
```

### 주문·결제·환불

```json
// OrderCreateRequest. 생략하거나 빈 배열이면 장바구니 전체 주문
{ "cartItemIds": [11, 12] }

// ChangeOrderStatusRequest
{ "status": "PAID" }

// PaymentConfirmRequest
{ "orderId": 101, "portonePaymentId": "pay_..." }

// CustomerRefundRequest
{
  "reason": "상품 일부가 파손되었습니다.",
  "items": [
    { "orderItemId": 11, "quantity": 1 }
  ]
}

// RefundRejectRequest
{ "reason": "환불 요청 정보를 확인할 수 없습니다." }
```

### 채팅

```json
// ChatRoomCreateRequest
{ "title": "배송 문의" }

// ChatRoomStatusUpdateRequest
{ "status": "IN_PROGRESS" }

// ChatMessageSendRequest
{ "message": "배송 일정이 궁금합니다." }
```

## 현재 기준 확인 사항

- `GET /api/products/**`, `GET /api/categories`, `/api/config/portone`, `/api/auth/**`, `/api/webhooks/portone`은 Security 설정에서 공개되어 있다.
- 상품·카테고리의 생성·수정·삭제와 관리자 주문 상태 변경은 관리자 역할을 확인한다.
- 채팅의 HTTP 연결 경로는 공개지만, STOMP CONNECT 단계에서 JWT를 검증한다.
- `POST /api/payments/confirm`은 요청 Idempotency-Key를 받지 않는다. 결제·웹훅 처리의 중복 상태 변경 방지는 별도 상태 전이와 웹훅 ID 처리로 수행한다.
