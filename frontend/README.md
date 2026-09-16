# 어니언즈 ONIONZ 프론트엔드

React + TypeScript + Vite로 구현한 양파 전문 쇼핑몰 프론트엔드입니다.

API 모듈은 실제 백엔드 저장소(`onion-store`, `Makgoli-SuyuK/onion-store` dev 브랜치)의 Controller/DTO를 직접 확인해서 경로·요청/응답 필드를 맞췄습니다. 다만 백엔드에 아직 없는 기능(상품 이미지, 좋아요 API, 배송지, 관리자 통계 등)은 아래 "백엔드에 없는 기능" 절에 정리했습니다.

## 실행 방법

```bash
npm install
cp .env.example .env   # 필요하면 값 수정
npm run dev             # http://localhost:5173
npm run build            # 프로덕션 빌드 (dist/)
npm run preview          # 빌드 결과 미리보기
```

## 환경변수 (.env.example)

```
# 백엔드 API 서버 주소. 실제 배포/로컬 백엔드 주소로 교체하세요.
VITE_API_BASE_URL=http://localhost:8080

# true면 src/mocks 임시 데이터로 동작(백엔드 없이 화면 확인용).
# 백엔드 연동 시 false로 바꾸세요.
VITE_USE_MOCK=true
```

`.env` 파일이 없어도 `VITE_USE_MOCK`은 기본값 `true`로 동작하므로, 백엔드 없이 바로 `npm run dev`로 전체 화면을 확인할 수 있습니다. 포트원(PortOne) 결제창을 여는 데 필요한 `storeId`/`channelKey`는 별도 환경변수가 아니라 백엔드의 `GET /api/config/portone`에서 그때그때 받아옵니다.

## 폴더 구조

```
src/
├─ api/             # 백엔드 API 호출 모듈 (컴포넌트에는 주소/요청 코드를 직접 쓰지 않음)
│  ├─ client.ts        # axios 인스턴스, JWT 헤더 부착, 공통 에러 정규화, ApiResponse/Page unwrap
│  ├─ productApi.ts    # 상품 목록(GET /api/products)/상세(GET /api/products/{id})
│  ├─ categoryApi.ts   # 카테고리 목록(GET /api/categories)
│  ├─ cartApi.ts       # 장바구니 조회/추가/수량변경/삭제 (/api/carts/...)
│  ├─ orderApi.ts      # 주문 생성/조회/내역 (/api/orders/...)
│  ├─ paymentApi.ts    # 포트원 설정 조회, 결제 완료 확인 (/api/config/portone, /api/payments/confirm)
│  ├─ authApi.ts       # 회원가입/로그인 (/api/auth/...)
│  ├─ userApi.ts       # 내 정보 조회 (/api/users/me)
│  ├─ adminApi.ts      # 관리자 상품 등록/수정, 통계(클라이언트 집계)
│  └─ chatApi.ts       # CS 문의 채팅방/메시지 REST (/api/chats/..., /api/admin/chats/...)
├─ payment/portone.ts # 포트원 V2 브라우저 SDK 연동 래퍼
├─ chat/stompClient.ts # 채팅 STOMP WebSocket 연결 래퍼 (/ws-stomp)
├─ mocks/           # 임시 데이터 (VITE_USE_MOCK=true일 때 api/*.ts가 이 모듈을 사용)
├─ types/           # 요청/응답 타입 (product, cart, order, user, category, payment, admin, common)
├─ hooks/           # 공통 상태 + 서버 요청 처리
│  ├─ useAuth.tsx        # 로그인 상태, JWT 저장/복원, 로그인/회원가입/로그아웃
│  ├─ useCart.tsx        # 장바구니 전역 상태(헤더 뱃지 등), 담기/수량변경/삭제
│  ├─ useCategories.tsx  # 카테고리 목록을 앱 시작 시 한 번 불러와 공유
│  ├─ useToast.tsx       # 토스트 알림
│  └─ useApiRequest.ts   # GET성 요청 공통 로딩/에러/데이터 상태 훅
├─ components/
│  ├─ layout/    # Header(공지+로고+검색+메뉴+카테고리), Footer, Layout
│  ├─ common/    # LoadingSpinner, EmptyState, ErrorState, Modal, ProductImage, QuantityStepper
│  ├─ product/   # ProductCard, ProductGrid
│  ├─ cart/      # CartItemRow
│  └─ admin/     # AdminStatCard, ProductFormModal
├─ pages/         # 화면 단위 (아래 "주요 페이지" 참고)
│  ├─ admin/AdminProductListPage.tsx
│  └─ chat/ChatListPage.tsx, ChatRoomPage.tsx
├─ routes/        # router.tsx(라우트 테이블), ProtectedRoute.tsx(로그인/관리자 가드)
├─ constants/     # orderStatus.ts(주문상태 라벨), chatStatus.ts(채팅상태 라벨)
├─ utils/         # currency.ts(원화 포맷), errorMessage.ts(에러→한글 메시지), storage.ts(토큰 저장)
├─ assets/        # onionz-character.png (첨부 캐릭터 이미지)
└─ styles/global.css  # 색상/폰트/반경/그림자 등 디자인 토큰 + 공통 버튼·입력·카드·토스트 스타일
```

## 백엔드 연동 위치

- 실제 API 주소는 `.env`의 `VITE_API_BASE_URL` 한 곳만 바꾸면 됩니다 (`src/api/client.ts`에서 axios `baseURL`로 사용).
- 각 `src/api/*.ts` 파일의 `http.get/post/patch/delete(...)` 호출부에 실제 경로가 나와 있습니다(아래 표 참고).
- 응답 포맷은 백엔드 공통 포맷 `ApiResponse(success, code, message, data)`를 그대로 따릅니다 (`src/types/common.ts`, `src/api/client.ts`의 `unwrap`/`unwrapPage`). 목록 API는 Spring Data `Page` 그대로 내려오며(`number`/`size` 필드), `unwrapPage`가 화면에서 쓰기 쉬운 모양(`page`/`size`)으로 바꿔줍니다.
- 인증이 필요한 요청은 `client.ts`의 axios 요청 인터셉터가 `localStorage`에 저장된 토큰을 `Authorization: Bearer {accessToken}` 헤더로 자동으로 붙입니다.
- **백엔드는 로그인(`/api/auth/**`)을 제외한 거의 모든 API에 로그인을 요구합니다** (`SecurityConfig`가 `/api/auth/**`, swagger, `/ws-stomp/**`만 permitAll이고 나머지는 `anyRequest().authenticated()`). 상품 목록처럼 비로그인 사용자도 봐야 자연스러운 화면까지 로그인 뒤에 있는 상태라, 이 프론트는 일단 상품 탐색은 비로그인으로 열어두고 장바구니/주문/마이페이지/관리자만 로그인으로 막았습니다. 백엔드 보안 설정이 최종 확정되면(상품 조회를 permitAll로 열 계획인지) 맞춰서 조정하면 됩니다.
- `VITE_USE_MOCK=false`로 바꾸면 모든 `api/*.ts` 모듈이 `src/mocks/*`를 거치지 않고 바로 실제 백엔드를 호출합니다.

### 실제로 확인한 엔드포인트

| 기능 | 메서드/경로 | 비고 |
|---|---|---|
| 회원가입 | `POST /api/auth/signup` | 토큰을 주지 않음 → 가입 후 로그인을 이어서 호출 |
| 로그인 | `POST /api/auth/login` | 응답에 이름/이메일 없음 (`accessToken`/`userId`/`role`만) |
| 내 정보 | `GET /api/users/me` | 로그인 응답에 없는 이름/이메일/전화번호는 여기서 |
| 카테고리 목록 | `GET /api/categories` | 카테고리는 고정 enum이 아니라 관리자가 만든 값 |
| 상품 목록 | `GET /api/products?name=&category=&sortBy=&sortOrder=&page=&size=` | 요약 응답(재고/판매상태 없음) |
| 상품 상세 | `GET /api/products/{id}` | 재고/판매상태 포함 |
| 상품 등록(관리자) | `POST /api/products` | `{category, productName, description, price, stock}` |
| 상품 수정(관리자) | `PATCH /api/products/{id}` | `{name, description, price, stock, status}` — 카테고리 변경 불가 |
| 장바구니 조회 | `GET /api/carts` | |
| 장바구니 담기 | `POST /api/carts/items` | |
| 수량 변경 | `PATCH /api/carts/items/{id}` | |
| 항목 삭제 | `DELETE /api/carts/items/{id}` | dev 브랜치에는 아직 병합 안 됨(`feature/cart-item-delete`) |
| 주문 생성 | `POST /api/orders` | `cartItemIds` 비우면 장바구니 전체 주문. 가격/재고/판매상태 검증을 이 호출이 원자적으로 수행 |
| 주문 상세/내역 | `GET /api/orders/{id}`, `GET /api/orders` | |
| 포트원 설정 | `GET /api/config/portone` | `{storeId, channelKey}` |
| 결제 완료 확인 | `POST /api/payments/confirm` | `{orderId, portonePaymentId}` |
| 문의방 생성 | `POST /api/chats/rooms` | 관리자는 생성 불가 |
| 문의방 목록/상세 | `GET /api/chats/rooms`, `GET /api/chats/rooms/{id}` | 고객=본인 것만, 관리자=전체(+상태 필터) |
| 문의 메시지 내역 | `GET /api/chats/rooms/{id}/messages?lastMessageId=&size=` | 커서 기반 페이징 |
| 문의 상태 변경(관리자) | `PATCH /api/admin/chats/rooms/{id}/status` | 완료 상태는 되돌릴 수 없음 |
| 실시간 메시지 | STOMP `/pub/chats/rooms/{id}/messages` 전송, `/sub/chats/rooms/{id}` 구독 | WebSocket 엔드포인트 `/ws-stomp` |

## 1:1 문의 채팅 (CS 문의)

구매자↔판매자 채팅은 백엔드에 해당 모델이 없어서 만들지 않았습니다. **고객↔관리자 CS 문의 채팅만** 구현했습니다.

- `POST/GET /api/chats/rooms`, `GET /api/chats/rooms/{id}`, `GET /api/chats/rooms/{id}/messages` (REST, 방 생성/목록/상세/메시지 내역)
- `PATCH /api/admin/chats/rooms/{id}/status` (관리자 전용 상태 변경: 대기중→상담중→완료, 완료는 되돌릴 수 없음)
- 실시간 메시지 전송/수신은 STOMP WebSocket (`/ws-stomp`, `@stomp/stompjs`). CONNECT 프레임의 `Authorization: Bearer {accessToken}` 헤더로 인증하고, `/pub/chats/rooms/{id}/messages`로 보내고 `/sub/chats/rooms/{id}`를 구독해서 받습니다 (`src/chat/stompClient.ts`).
- 헤더의 "1:1 문의"(고객)/"문의 관리"(관리자) 링크 → `/chat` (내 문의 목록, 관리자는 상태별 필터+전체 목록) → `/chat/rooms/:id` (채팅방)
- `VITE_USE_MOCK=true`일 때는 실제 WebSocket을 열지 않고 `mocks/chatMock.ts`가 전송을 흉내 냅니다.

## 결제(포트원) 흐름

주문서에 주소/결제수단 입력창이 없습니다 — 백엔드에 그런 필드가 아예 없고, 실제 결제는 **포트원(PortOne)** 결제창으로 처리됩니다.

1. `POST /api/orders`로 주문 생성 → `orderId`, `portonePaymentId`, `totalPrice`를 받음
2. `GET /api/config/portone`으로 `storeId`/`channelKey` 조회
3. `src/payment/portone.ts`가 포트원 V2 브라우저 SDK(`window.PortOne.requestPayment`)로 결제창을 염 (SDK는 `index.html`에서 `https://cdn.portone.io/v2/browser-sdk.js`로 로드)
4. 결제가 끝나면 `POST /api/payments/confirm`으로 결제 완료를 서버에 알리고 주문 완료 화면으로 이동

`VITE_USE_MOCK=true`(기본값)일 때는 포트원 결제창을 열지 않고 바로 결제 완료로 처리합니다. 실제 결제를 테스트하려면 `VITE_USE_MOCK=false` + 실제 백엔드 + 포트원 스토어 설정이 필요합니다.

## 백엔드에 없는 기능 (TODO)

아래는 첨부 요구사항에는 있지만 현재 백엔드에는 없어서, 화면은 만들어 두되 실제로는 동작하지 않거나 프론트에서만 계산하는 부분입니다.

- **상품 이미지**: 상품 응답에 이미지 필드 자체가 없습니다. `ProductImage` 컴포넌트가 항상 "상품 사진 준비 중" placeholder를 보여줍니다.
- **좋아요(찜) 등록/취소 API**: 에러코드(`LIKE_001/002`)만 준비돼 있고 실제 등록/취소 엔드포인트는 아직 없습니다. 좋아요 버튼은 화면에서만 숫자가 올라가고 새로고침하면 초기화됩니다.
- **배송비/배송지**: 주문에 배송비·주소 개념이 없습니다. 장바구니/주문서의 "배송비"는 화면 안내용 계산일 뿐 실제 결제 금액(`totalPrice`)에는 포함되지 않는다고 명시해 뒀습니다.
- **상품 목록의 재고/판매상태**: 목록 API가 요약 정보만 줘서 재고/품절 여부를 알 수 없습니다. 상품 상세에서는 정상적으로 보입니다. 관리자 화면은 행마다 상세를 추가로 조회해 채웁니다(`adminApi.ts`의 `fetchProductsWithDetail`, 상품이 많아지면 느려질 수 있어 목록 API 확장이 필요합니다).
- **관리자 통계 API**: 없어서 상품을 한 번에 많이 불러와 프론트에서 집계합니다.
- **주문 상태**: `PENDING/PAID/CANCELLED` 3가지뿐이라 "배송중/배송완료" 같은 배송 추적은 없습니다.
- **주문 취소**: 백엔드에는 `POST /api/orders/{id}/cancel`가 있지만(결제 전에만 가능) 원래 요구사항에 없던 기능이라 화면은 만들지 않았습니다.

## 임시 데이터(mock)

`VITE_USE_MOCK=true`(기본값)일 때 `src/mocks/`의 인메모리 데이터로 동작합니다. 백엔드 연동 시 `VITE_USE_MOCK=false`로 바꾸면 자동으로 실제 API를 호출합니다.

- `mocks/products.ts` — 상품 10종(카테고리별, 일부 품절 포함)
- `mocks/categories.ts` — 카테고리 5종(양파/햇양파/자색양파/깐양파/양파즙)
- `mocks/cartMock.ts`, `mocks/orderMock.ts` — 페이지를 새로고침하면 초기화되는 인메모리 데이터입니다(브라우저 탭을 유지한 채 SPA 링크로 이동하면 유지됩니다). 주문 내역에는 데모용 과거 주문 2건이 미리 들어 있습니다.
- `mocks/authMock.ts` — 비밀번호 검증 없이, 이메일에 `admin`이 포함되면 관리자 계정으로 로그인/가입됩니다.
- `mocks/adminMock.ts` — 관리자 등록/수정이 `products.ts` 배열을 직접 수정합니다.

## 주요 페이지 구현 설명

- **HomePage (`/`)**: 캐릭터 이미지(`object-fit: contain`)를 히어로 영역에 배치, 메인/보조 문구, CTA 버튼, 서비스 특징 6개, 인기 상품 4개(`productApi.getProducts({sort:'POPULAR', size:4})`, 인기순=좋아요수 내림차순).
- **ProductListPage (`/products`)**: 검색어(`name`)/카테고리/정렬을 URL 쿼리(`useSearchParams`)로 관리해 새로고침·뒤로가기에도 상태가 유지됩니다. 카테고리는 `useCategories`로 백엔드에서 받아온 목록을 그대로 씁니다. "최신순"은 백엔드에 생성일 정렬이 없어 기본 순서로 대체됩니다. 로딩/에러/빈 결과 상태 모두 구현.
- **ProductDetailPage (`/products/:id`)**: 수량은 1~재고 범위로만 조절되며, 품절 시 구매 버튼이 비활성화됩니다. 장바구니 담기/바로구매 모두 비로그인 시 로그인 페이지로 안내합니다.
- **CartPage (`/cart`)**: 전체/개별 선택, 수량 증감, 개별 삭제, 선택 금액·배송비(안내용)·총 결제 예정 금액을 계산합니다.
- **CheckoutPage (`/checkout`)**: 주문 상품과 로그인 계정의 주문자 정보(이름/연락처/이메일)를 보여준 뒤, 결제하기를 누르면 주문 생성 → 포트원 결제창 → 결제 완료 확인까지 이어집니다. 재고 부족/품절 등은 주문 생성 시점에 서버가 원자적으로 검증해 에러로 내려줍니다.
- **OrderCompletePage (`/orders/complete/:orderId`)**: 주문번호/일시/결제금액과 "주문 내역 보기"/"쇼핑 계속하기" 버튼.
- **MyOrdersPage / OrderDetailPage (`/mypage/orders`, `/mypage/orders/:orderId`)**: 주문 목록과 상세 내역(로그인 필요). 목록 API에는 주문 상태가 없어 결제 완료 여부(`paidAt` 존재)로만 배지를 표시합니다.
- **AdminProductListPage (`/admin/products`, 관리자 전용)**: 전체/판매중/품절 통계 카드, 상품명 검색, 상품 등록/수정 모달. 표 형태로 가격·재고·판매상태를 관리합니다.
- **ChatListPage / ChatRoomPage (`/chat`, `/chat/rooms/:roomId`)**: 고객은 문의 생성 + 내 문의 목록, 관리자는 전체 문의 목록(상태 필터)을 봅니다. 채팅방에서는 실시간 메시지 송수신과(관리자만) 상태 변경이 가능합니다.
- **로그인/회원가입 (`/login`, `/signup`)**: 회원가입은 이름/이메일/전화번호/비밀번호를 받고, 가입 성공 시 자동으로 로그인까지 이어집니다.

## 오류 처리

`src/utils/errorMessage.ts`가 백엔드 오류 코드/영문 메시지를 화면에 직접 노출하지 않고 한글 메시지로 변환합니다. 백엔드 `ErrorCode`가 이미 모든 코드에 한글 `message`를 내려주므로 그 값을 우선 사용하고, 코드 매핑표(`onion-store`의 실제 `ErrorCode.java` 기준)는 message가 비어있을 때의 안전장치로만 씁니다. 모든 오류는 alert 대신 토스트(`useToast`) 또는 화면 내 안내 문구로 표시됩니다.

## 참고

- 접근성: 아이콘 버튼에 `aria-label`, 이미지 미제공 시 대체 텍스트("상품 사진 준비 중"), 폼 라벨(`label`+`id`) 연결.
- 금액은 `Intl.NumberFormat('ko-KR', {style:'currency', currency:'KRW'})`로 표시합니다.
- 첨부된 캐릭터 이미지 원본은 `src/assets/onionz-character.png`이며 용량이 커서(약 1.8MB) 실제 배포 전에는 압축/리사이즈를 권장합니다.
