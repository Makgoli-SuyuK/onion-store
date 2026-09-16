INSERT INTO users (email, password, name, phone_number, role, status, created_at, updated_at)
SELECT 'admin@onion.store',
       '$2y$10$0sCj0rqVo3QYOz8M5RIj3OX4jKE726oKhqS8K475mc/xnxD7dXJ/G',
       '관리자', '01000000000', 'ADMIN', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@onion.store');

INSERT INTO categories (name, deleted, created_at, updated_at)
SELECT '결제 테스트', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = '결제 테스트');

INSERT INTO products (category_id, name, description, price, stock, like_count, status, deleted, created_at, updated_at)
SELECT category.id, '결제 테스트 양파즙', '포트원 결제 흐름 확인용 상품', 1000, 100, 0, 'SELLING', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM categories category
WHERE category.name = '결제 테스트'
  AND NOT EXISTS (SELECT 1 FROM products WHERE name = '결제 테스트 양파즙');

-- 환불 API 수동 테스트 계정 (비밀번호: refund1234)
INSERT INTO users (email, password, name, phone_number, role, status, created_at, updated_at)
SELECT 'refund-admin@onion.store',
       '$2y$10$OcDF.mF/qlMwwiSYcYJqp.9Jyv2Ohr8ZPY5Y0LqkjiBpMc8XFxRPa',
       '환불테스트관리자', '01011112222', 'ADMIN', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'refund-admin@onion.store');

INSERT INTO users (email, password, name, phone_number, role, status, created_at, updated_at)
SELECT 'refund-customer@onion.store',
       '$2y$10$OcDF.mF/qlMwwiSYcYJqp.9Jyv2Ohr8ZPY5Y0LqkjiBpMc8XFxRPa',
       '환불테스트고객', '01033334444', 'CUSTOMER', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'refund-customer@onion.store');

INSERT INTO users (email, password, name, phone_number, role, status, created_at, updated_at)
SELECT 'other-customer@onion.store',
       '$2y$10$OcDF.mF/qlMwwiSYcYJqp.9Jyv2Ohr8ZPY5Y0LqkjiBpMc8XFxRPa',
       '다른고객', '01055556666', 'CUSTOMER', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'other-customer@onion.store');

-- 부분 환불 수량·금액을 구분해 확인할 수 있는 테스트 상품
INSERT INTO products (category_id, name, description, price, stock, like_count, status, deleted, created_at, updated_at)
SELECT category.id, '환불 테스트 양파즙 A', '환불 요청 및 수량 검증용 상품', 1000, 100, 0, 'SELLING', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM categories category
WHERE category.name = '결제 테스트'
  AND NOT EXISTS (SELECT 1 FROM products WHERE name = '환불 테스트 양파즙 A');

INSERT INTO products (category_id, name, description, price, stock, like_count, status, deleted, created_at, updated_at)
SELECT category.id, '환불 테스트 양파즙 B', '부분 환불 금액 검증용 상품', 2000, 100, 0, 'SELLING', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM categories category
WHERE category.name = '결제 테스트'
  AND NOT EXISTS (SELECT 1 FROM products WHERE name = '환불 테스트 양파즙 B');

INSERT INTO products (category_id, name, description, price, stock, like_count, status, deleted, created_at, updated_at)
SELECT category.id, '환불 테스트 양파즙 C', '환불 상태 조회용 상품', 3000, 100, 0, 'SELLING', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM categories category
WHERE category.name = '결제 테스트'
  AND NOT EXISTS (SELECT 1 FROM products WHERE name = '환불 테스트 양파즙 C');

-- 1. 완료 환불 이력이 없는 주문: 첫 부분·전액 환불 요청 테스트
INSERT INTO orders (order_number, user_id, total_price, status, created_at, updated_at)
SELECT 'ORD-REFUND-READY-001', user.id, 7000, 'PAID', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM users user
WHERE user.email = 'refund-customer@onion.store'
  AND NOT EXISTS (SELECT 1 FROM orders WHERE order_number = 'ORD-REFUND-READY-001');

INSERT INTO order_items (order_id, product_id, product_name, product_price, quantity, created_at)
SELECT orders.id, products.id, products.name, products.price, 3, CURRENT_TIMESTAMP
FROM orders
JOIN products ON products.name = '환불 테스트 양파즙 A'
WHERE orders.order_number = 'ORD-REFUND-READY-001'
  AND NOT EXISTS (
      SELECT 1 FROM order_items
      WHERE order_id = orders.id AND product_id = products.id
  );

INSERT INTO order_items (order_id, product_id, product_name, product_price, quantity, created_at)
SELECT orders.id, products.id, products.name, products.price, 2, CURRENT_TIMESTAMP
FROM orders
JOIN products ON products.name = '환불 테스트 양파즙 B'
WHERE orders.order_number = 'ORD-REFUND-READY-001'
  AND NOT EXISTS (
      SELECT 1 FROM order_items
      WHERE order_id = orders.id AND product_id = products.id
  );

INSERT INTO payments (order_id, portone_payment_id, amount, status, paid_at, created_at, updated_at)
SELECT orders.id, 'pay_refund_ready_001', 7000, 'SUCCESS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM orders
WHERE orders.order_number = 'ORD-REFUND-READY-001'
  AND NOT EXISTS (SELECT 1 FROM payments WHERE order_id = orders.id);

-- 2. 일부 완료 환불이 있는 주문: 남은 수량 검증과 추가 부분 환불 테스트
INSERT INTO orders (order_number, user_id, total_price, status, created_at, updated_at)
SELECT 'ORD-REFUND-PARTIAL-001', user.id, 7000, 'PAID', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM users user
WHERE user.email = 'refund-customer@onion.store'
  AND NOT EXISTS (SELECT 1 FROM orders WHERE order_number = 'ORD-REFUND-PARTIAL-001');

INSERT INTO order_items (order_id, product_id, product_name, product_price, quantity, created_at)
SELECT orders.id, products.id, products.name, products.price, 4, CURRENT_TIMESTAMP
FROM orders
JOIN products ON products.name = '환불 테스트 양파즙 A'
WHERE orders.order_number = 'ORD-REFUND-PARTIAL-001'
  AND NOT EXISTS (
      SELECT 1 FROM order_items
      WHERE order_id = orders.id AND product_id = products.id
  );

INSERT INTO order_items (order_id, product_id, product_name, product_price, quantity, created_at)
SELECT orders.id, products.id, products.name, products.price, 1, CURRENT_TIMESTAMP
FROM orders
JOIN products ON products.name = '환불 테스트 양파즙 C'
WHERE orders.order_number = 'ORD-REFUND-PARTIAL-001'
  AND NOT EXISTS (
      SELECT 1 FROM order_items
      WHERE order_id = orders.id AND product_id = products.id
  );

INSERT INTO payments (order_id, portone_payment_id, amount, status, paid_at, created_at, updated_at)
SELECT orders.id, 'pay_refund_partial_001', 7000, 'PARTIALLY_CANCELLED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM orders
WHERE orders.order_number = 'ORD-REFUND-PARTIAL-001'
  AND NOT EXISTS (SELECT 1 FROM payments WHERE order_id = orders.id);

INSERT INTO refunds (payment_id, amount, status, initiator, reason_type, reason, reviewed_by, reviewed_at, portone_cancellation_id, requested_cancellable_amount, created_at, updated_at)
SELECT payments.id, 1000, 'COMPLETED', 'CUSTOMER', 'CUSTOMER_REQUEST', '테스트 완료 부분 환불', admin.id, CURRENT_TIMESTAMP, 'cancel_refund_completed_001', 7000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM payments
JOIN users admin ON admin.email = 'refund-admin@onion.store'
WHERE payments.portone_payment_id = 'pay_refund_partial_001'
  AND NOT EXISTS (SELECT 1 FROM refunds WHERE reason = '테스트 완료 부분 환불');

INSERT INTO refund_items (refund_id, order_item_id, quantity)
SELECT refunds.id, order_items.id, 1
FROM refunds
JOIN payments ON payments.id = refunds.payment_id
JOIN orders ON orders.id = payments.order_id
JOIN order_items ON order_items.order_id = orders.id
WHERE refunds.reason = '테스트 완료 부분 환불'
  AND order_items.product_name = '환불 테스트 양파즙 A'
  AND NOT EXISTS (
      SELECT 1 FROM refund_items
      WHERE refund_id = refunds.id AND order_item_id = order_items.id
  );

-- 로컬 화면 확인용 카테고리: 상품 목록·필터를 다양한 값으로 확인한다.
INSERT INTO categories (name, deleted, created_at, updated_at)
SELECT seed.name, false, seed.created_at, seed.created_at
FROM (
    SELECT '햇양파' AS name, '2026-02-10 09:00:00' AS created_at
    UNION ALL SELECT '자색양파', '2026-03-05 10:30:00'
    UNION ALL SELECT '깐양파', '2026-04-12 11:15:00'
    UNION ALL SELECT '양파즙', '2026-05-08 14:00:00'
    UNION ALL SELECT '선물세트', '2026-06-01 09:45:00'
    UNION ALL SELECT '조미·가공', '2026-07-03 16:20:00'
) seed
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = seed.name);

-- 로컬 화면 확인용 상품: 판매중·품절·숨김 상태와 가격대, 좋아요 수를 섞는다.
INSERT INTO products (category_id, name, description, price, stock, like_count, status, deleted, created_at, updated_at)
SELECT category.id,
       seed.name,
       seed.description,
       seed.price,
       seed.stock,
       seed.like_count,
       seed.status,
       false,
       seed.created_at,
       seed.updated_at
FROM (
         SELECT
             '햇양파' AS category_name,'2026 햇양파 3kg' AS name,'아삭하고 단맛이 좋은 햇양파입니다.' AS description,12900 AS price,84 AS stock,37 AS like_count,'SELLING' AS status,'2026-02-12 09:30:00' AS created_at,
             '2026-08-25 10:10:00' AS updated_at    UNION ALL SELECT '햇양파', '2026 햇양파 5kg', '가정용으로 넉넉하게 담은 햇양파입니다.', 18900, 42, 21, 'SELLING', '2026-02-15 11:20:00', '2026-08-22 15:00:00'
    UNION ALL SELECT '자색양파', '자색 양파 2kg', '샐러드와 피클에 잘 어울리는 자색 양파입니다.', 10900, 63, 18, 'SELLING', '2026-03-08 10:00:00', '2026-08-18 13:25:00'
    UNION ALL SELECT '자색양파', '자색 양파 5kg', '풍부한 색감과 단맛을 가진 대용량 자색 양파입니다.', 20900, 0, 46, 'SOLD_OUT', '2026-03-12 14:40:00', '2026-08-29 09:15:00'
    UNION ALL SELECT '깐양파', '깐 양파 1kg', '손질 없이 바로 조리할 수 있는 깐 양파입니다.', 8900, 120, 52, 'SELLING', '2026-04-15 08:50:00', '2026-08-20 16:30:00'
    UNION ALL SELECT '깐양파', '깐 양파 3kg', '업소와 대가족을 위한 깐 양파 대용량입니다.', 21900, 28, 14, 'SELLING', '2026-04-18 13:10:00', '2026-08-24 11:45:00'
    UNION ALL SELECT '양파즙', '무첨가 양파즙 30포', '국내산 양파를 달여 만든 무첨가 양파즙입니다.', 15900, 77, 65, 'SELLING', '2026-05-10 10:20:00', '2026-08-30 12:00:00'
    UNION ALL SELECT '양파즙', '흑마늘 양파즙 30포', '흑마늘을 더해 깊은 맛을 낸 양파즙입니다.', 19900, 35, 29, 'SELLING', '2026-05-14 15:35:00', '2026-08-27 17:10:00'
    UNION ALL SELECT '선물세트', '햇양파 선물세트 3호', '감사 인사를 전하기 좋은 햇양파 선물세트입니다.', 32900, 16, 33, 'SELLING', '2026-06-04 09:10:00', '2026-08-26 14:20:00'
    UNION ALL SELECT '선물세트', '프리미엄 양파 혼합세트', '햇양파와 자색 양파를 함께 담은 프리미엄 세트입니다.', 42900, 8, 41, 'SELLING', '2026-06-09 11:55:00', '2026-08-28 10:40:00'
    UNION ALL SELECT '조미·가공', '양파볶음 2병', '볶음밥과 카레에 바로 넣기 좋은 양파볶음입니다.', 12900, 54, 12, 'SELLING', '2026-07-06 14:25:00', '2026-08-21 09:50:00'
    UNION ALL SELECT '조미·가공', '양파장아찌 1kg', '아삭한 식감의 달콤새콤 양파장아찌입니다.', 10900, 0, 9, 'SOLD_OUT', '2026-07-11 16:05:00', '2026-08-31 08:30:00'
    UNION ALL SELECT '조미·가공', '양파 수프 베이스', '신제품 출시 전 내부 확인용 상품입니다.', 7900, 40, 3, 'HIDDEN', '2026-08-01 10:00:00', '2026-08-31 11:30:00'
) seed
JOIN categories category ON category.name = seed.category_name
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = seed.name);

-- 환불테스트고객(refund1234)이 주문 내역 화면에서 확인할 수 있는 일반 주문 9건.
INSERT INTO orders (order_number, user_id, total_price, status, cancelled_at, created_at, updated_at)
SELECT seed.order_number,
       user.id,
       seed.total_price,
       seed.status,
       seed.cancelled_at,
       seed.created_at,
       seed.updated_at
FROM (
    SELECT 'ORD-DEMO-20260831-001' AS order_number, 31800 AS total_price, 'PAID' AS status, NULL AS cancelled_at, '2026-08-31 13:20:00' AS created_at, '2026-08-31 13:23:00' AS updated_at
    UNION ALL SELECT 'ORD-DEMO-20260718-001', 45700, 'PAID', NULL, '2026-07-18 18:40:00', '2026-07-18 18:44:00'
    UNION ALL SELECT 'ORD-DEMO-20260602-001', 17800, 'PAID', NULL, '2026-06-02 09:15:00', '2026-06-02 09:19:00'
    UNION ALL SELECT 'ORD-DEMO-20260516-001', 21800, 'CANCELLED', '2026-05-16 20:05:00', '2026-05-16 19:42:00', '2026-05-16 20:05:00'
    UNION ALL SELECT 'ORD-DEMO-20260411-001', 26700, 'CANCELLED', '2026-04-15 11:20:00', '2026-04-11 10:10:00', '2026-04-15 11:20:00'
    UNION ALL SELECT 'ORD-DEMO-20260327-001', 25800, 'PAID', NULL, '2026-03-27 16:30:00', '2026-03-30 14:10:00'
    UNION ALL SELECT 'ORD-DEMO-20260214-001', 19900, 'PENDING', NULL, '2026-02-14 12:05:00', '2026-02-14 12:05:00'
    UNION ALL SELECT 'ORD-DEMO-20260120-001', 38700, 'PAID', NULL, '2026-01-20 08:55:00', '2026-01-21 09:10:00'
    UNION ALL SELECT 'ORD-DEMO-20251224-001', 32900, 'PAID', NULL, '2025-12-24 17:20:00', '2025-12-24 17:25:00'
) seed
JOIN users user ON user.email = 'refund-customer@onion.store'
WHERE NOT EXISTS (SELECT 1 FROM orders WHERE order_number = seed.order_number);

INSERT INTO order_items (order_id, product_id, product_name, product_price, quantity, created_at)
SELECT orders.id,
       products.id,
       products.name,
       products.price,
       seed.quantity,
       seed.created_at
FROM (
    SELECT 'ORD-DEMO-20260831-001' AS order_number, '2026 햇양파 3kg' AS product_name, 1 AS quantity, '2026-08-31 13:20:00' AS created_at
    UNION ALL SELECT 'ORD-DEMO-20260831-001', '2026 햇양파 5kg', 1, '2026-08-31 13:20:00'
    UNION ALL SELECT 'ORD-DEMO-20260718-001', '자색 양파 5kg', 1, '2026-07-18 18:40:00'
    UNION ALL SELECT 'ORD-DEMO-20260718-001', '무첨가 양파즙 30포', 1, '2026-07-18 18:40:00'
    UNION ALL SELECT 'ORD-DEMO-20260718-001', '깐 양파 1kg', 1, '2026-07-18 18:40:00'
    UNION ALL SELECT 'ORD-DEMO-20260602-001', '깐 양파 1kg', 2, '2026-06-02 09:15:00'
    UNION ALL SELECT 'ORD-DEMO-20260516-001', '자색 양파 2kg', 2, '2026-05-16 19:42:00'
    UNION ALL SELECT 'ORD-DEMO-20260411-001', '깐 양파 1kg', 3, '2026-04-11 10:10:00'
    UNION ALL SELECT 'ORD-DEMO-20260327-001', '2026 햇양파 3kg', 2, '2026-03-27 16:30:00'
    UNION ALL SELECT 'ORD-DEMO-20260214-001', '흑마늘 양파즙 30포', 1, '2026-02-14 12:05:00'
    UNION ALL SELECT 'ORD-DEMO-20260120-001', '2026 햇양파 3kg', 3, '2026-01-20 08:55:00'
    UNION ALL SELECT 'ORD-DEMO-20251224-001', '햇양파 선물세트 3호', 1, '2025-12-24 17:20:00'
) seed
JOIN orders ON orders.order_number = seed.order_number
JOIN products ON products.name = seed.product_name
WHERE NOT EXISTS (
    SELECT 1
    FROM order_items
    WHERE order_id = orders.id AND product_id = products.id
);

INSERT INTO payments (order_id, portone_payment_id, amount, status, paid_at, created_at, updated_at)
SELECT orders.id,
       seed.portone_payment_id,
       seed.amount,
       seed.status,
       seed.paid_at,
       seed.created_at,
       seed.updated_at
FROM (
    SELECT 'ORD-DEMO-20260831-001' AS order_number, 'pay_demo_20260831_001' AS portone_payment_id, 31800 AS amount, 'SUCCESS' AS status, '2026-08-31 13:23:00' AS paid_at, '2026-08-31 13:20:00' AS created_at, '2026-08-31 13:23:00' AS updated_at
    UNION ALL SELECT 'ORD-DEMO-20260718-001', 'pay_demo_20260718_001', 45700, 'SUCCESS', '2026-07-18 18:44:00', '2026-07-18 18:40:00', '2026-07-18 18:44:00'
    UNION ALL SELECT 'ORD-DEMO-20260602-001', 'pay_demo_20260602_001', 17800, 'SUCCESS', '2026-06-02 09:19:00', '2026-06-02 09:15:00', '2026-06-02 09:19:00'
    UNION ALL SELECT 'ORD-DEMO-20260516-001', 'pay_demo_20260516_001', 21800, 'CANCELLED', '2026-05-16 19:46:00', '2026-05-16 19:42:00', '2026-05-16 20:05:00'
    UNION ALL SELECT 'ORD-DEMO-20260411-001', 'pay_demo_20260411_001', 26700, 'CANCELLED', '2026-04-11 10:13:00', '2026-04-11 10:10:00', '2026-04-15 11:20:00'
    UNION ALL SELECT 'ORD-DEMO-20260327-001', 'pay_demo_20260327_001', 25800, 'PARTIALLY_CANCELLED', '2026-03-27 16:34:00', '2026-03-27 16:30:00', '2026-03-30 14:10:00'
    UNION ALL SELECT 'ORD-DEMO-20260214-001', 'pay_demo_20260214_001', 19900, 'READY', NULL, '2026-02-14 12:05:00', '2026-02-14 12:05:00'
    UNION ALL SELECT 'ORD-DEMO-20260120-001', 'pay_demo_20260120_001', 38700, 'SUCCESS', '2026-01-20 08:58:00', '2026-01-20 08:55:00', '2026-01-21 09:10:00'
    UNION ALL SELECT 'ORD-DEMO-20251224-001', 'pay_demo_20251224_001', 32900, 'SUCCESS', '2025-12-24 17:25:00', '2025-12-24 17:20:00', '2025-12-27 13:40:00'
) seed
JOIN orders ON orders.order_number = seed.order_number
WHERE NOT EXISTS (SELECT 1 FROM payments WHERE order_id = orders.id);

-- 화면에서 환불 상태별 이력을 확인할 수 있도록 요청·대기·완료·거절·실패를 추가한다.
INSERT INTO refunds (payment_id, amount, status, initiator, reason_type, reason, reviewed_by, reviewed_at, rejection_reason, portone_cancellation_id, requested_cancellable_amount, created_at, updated_at)
SELECT payments.id,
       seed.amount,
       seed.status,
       'CUSTOMER',
       'CUSTOMER_REQUEST',
       seed.reason,
       CASE WHEN seed.reviewed_at IS NULL THEN NULL ELSE admin.id END,
       seed.reviewed_at,
       seed.rejection_reason,
       seed.portone_cancellation_id,
       seed.requested_cancellable_amount,
       seed.created_at,
       seed.updated_at
FROM (
    SELECT 'pay_demo_20260718_001' AS payment_id, 15900 AS amount, 'REQUESTED' AS status, '단순 변심으로 양파즙만 환불 요청' AS reason, '2026-07-19 09:30:00' AS reviewed_at, NULL AS rejection_reason, 'cancel_demo_requested_001' AS portone_cancellation_id, 45700 AS requested_cancellable_amount, '2026-07-19 09:00:00' AS created_at, '2026-07-19 09:30:00' AS updated_at
    UNION ALL SELECT 'pay_demo_20260602_001', 8900, 'PENDING_APPROVAL', '상품 상태 확인 후 환불 요청', NULL, NULL, NULL, NULL, '2026-06-04 14:20:00', '2026-06-04 14:20:00'
    UNION ALL SELECT 'pay_demo_20260411_001', 26700, 'COMPLETED', '전체 상품 반품 완료', '2026-04-14 16:10:00', NULL, 'cancel_demo_completed_001', 26700, '2026-04-13 11:00:00', '2026-04-15 11:20:00'
    UNION ALL SELECT 'pay_demo_20260327_001', 12900, 'COMPLETED', '주문 수량 중 1개 부분 환불', '2026-03-29 10:30:00', NULL, 'cancel_demo_partial_001', 25800, '2026-03-28 09:15:00', '2026-03-30 14:10:00'
    UNION ALL SELECT 'pay_demo_20260120_001', 12900, 'REJECTED', '보관 기간 경과 후 환불 요청', '2026-01-21 09:10:00', '환불 가능 기간이 지났습니다.', NULL, NULL, '2026-01-21 08:30:00', '2026-01-21 09:10:00'
    UNION ALL SELECT 'pay_demo_20251224_001', 32900, 'FAILED', '결제 수단 오류로 환불 재시도 필요', '2025-12-27 13:00:00', NULL, 'cancel_demo_failed_001', 32900, '2025-12-27 12:30:00', '2025-12-27 13:40:00'
) seed
JOIN payments ON payments.portone_payment_id = seed.payment_id
LEFT JOIN users admin ON admin.email = 'refund-admin@onion.store'
WHERE NOT EXISTS (SELECT 1 FROM refunds WHERE reason = seed.reason);

INSERT INTO refund_items (refund_id, order_item_id, quantity)
SELECT refunds.id,
       order_items.id,
       seed.quantity
FROM (
    SELECT '단순 변심으로 양파즙만 환불 요청' AS reason, '무첨가 양파즙 30포' AS product_name, 1 AS quantity
    UNION ALL SELECT '상품 상태 확인 후 환불 요청', '깐 양파 1kg', 1
    UNION ALL SELECT '전체 상품 반품 완료', '깐 양파 1kg', 3
    UNION ALL SELECT '주문 수량 중 1개 부분 환불', '2026 햇양파 3kg', 1
    UNION ALL SELECT '보관 기간 경과 후 환불 요청', '2026 햇양파 3kg', 1
    UNION ALL SELECT '결제 수단 오류로 환불 재시도 필요', '햇양파 선물세트 3호', 1
) seed
JOIN refunds ON refunds.reason = seed.reason
JOIN payments ON payments.id = refunds.payment_id
JOIN orders ON orders.id = payments.order_id
JOIN order_items ON order_items.order_id = orders.id
JOIN products ON products.id = order_items.product_id AND products.name = seed.product_name
WHERE NOT EXISTS (
    SELECT 1
    FROM refund_items
    WHERE refund_id = refunds.id AND order_item_id = order_items.id
);

-- 3. 승인 대기 환불이 있는 주문: 중복 진행 환불 차단과 관리자 목록 테스트
INSERT INTO orders (order_number, user_id, total_price, status, created_at, updated_at)
SELECT 'ORD-REFUND-PENDING-001', user.id, 7000, 'PAID', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM users user
WHERE user.email = 'refund-customer@onion.store'
  AND NOT EXISTS (SELECT 1 FROM orders WHERE order_number = 'ORD-REFUND-PENDING-001');

INSERT INTO order_items (order_id, product_id, product_name, product_price, quantity, created_at)
SELECT orders.id, products.id, products.name, products.price, 2, CURRENT_TIMESTAMP
FROM orders
JOIN products ON products.name = '환불 테스트 양파즙 B'
WHERE orders.order_number = 'ORD-REFUND-PENDING-001'
  AND NOT EXISTS (
      SELECT 1 FROM order_items
      WHERE order_id = orders.id AND product_id = products.id
  );

INSERT INTO order_items (order_id, product_id, product_name, product_price, quantity, created_at)
SELECT orders.id, products.id, products.name, products.price, 1, CURRENT_TIMESTAMP
FROM orders
JOIN products ON products.name = '환불 테스트 양파즙 C'
WHERE orders.order_number = 'ORD-REFUND-PENDING-001'
  AND NOT EXISTS (
      SELECT 1 FROM order_items
      WHERE order_id = orders.id AND product_id = products.id
  );

INSERT INTO payments (order_id, portone_payment_id, amount, status, paid_at, created_at, updated_at)
SELECT orders.id, 'pay_refund_pending_001', 7000, 'SUCCESS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM orders
WHERE orders.order_number = 'ORD-REFUND-PENDING-001'
  AND NOT EXISTS (SELECT 1 FROM payments WHERE order_id = orders.id);

INSERT INTO refunds (payment_id, amount, status, initiator, reason_type, reason, created_at, updated_at)
SELECT payments.id, 2000, 'PENDING_APPROVAL', 'CUSTOMER', 'CUSTOMER_REQUEST', '테스트 승인 대기 환불', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM payments
WHERE payments.portone_payment_id = 'pay_refund_pending_001'
  AND NOT EXISTS (SELECT 1 FROM refunds WHERE reason = '테스트 승인 대기 환불');

INSERT INTO refund_items (refund_id, order_item_id, quantity)
SELECT refunds.id, order_items.id, 1
FROM refunds
JOIN payments ON payments.id = refunds.payment_id
JOIN orders ON orders.id = payments.order_id
JOIN order_items ON order_items.order_id = orders.id
WHERE refunds.reason = '테스트 승인 대기 환불'
  AND order_items.product_name = '환불 테스트 양파즙 B'
  AND NOT EXISTS (
      SELECT 1 FROM refund_items
      WHERE refund_id = refunds.id AND order_item_id = order_items.id
  );

-- 4. 거절·실패 이력: 고객 이력과 관리자 상태 필터 테스트
INSERT INTO orders (order_number, user_id, total_price, status, created_at, updated_at)
SELECT 'ORD-REFUND-REJECTED-001', user.id, 5000, 'PAID', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM users user
WHERE user.email = 'refund-customer@onion.store'
  AND NOT EXISTS (SELECT 1 FROM orders WHERE order_number = 'ORD-REFUND-REJECTED-001');

INSERT INTO order_items (order_id, product_id, product_name, product_price, quantity, created_at)
SELECT orders.id, products.id, products.name, products.price, 2, CURRENT_TIMESTAMP
FROM orders
JOIN products ON products.name = '환불 테스트 양파즙 A'
WHERE orders.order_number = 'ORD-REFUND-REJECTED-001'
  AND NOT EXISTS (
      SELECT 1 FROM order_items
      WHERE order_id = orders.id AND product_id = products.id
  );

INSERT INTO order_items (order_id, product_id, product_name, product_price, quantity, created_at)
SELECT orders.id, products.id, products.name, products.price, 1, CURRENT_TIMESTAMP
FROM orders
JOIN products ON products.name = '환불 테스트 양파즙 C'
WHERE orders.order_number = 'ORD-REFUND-REJECTED-001'
  AND NOT EXISTS (
      SELECT 1 FROM order_items
      WHERE order_id = orders.id AND product_id = products.id
  );

INSERT INTO payments (order_id, portone_payment_id, amount, status, paid_at, created_at, updated_at)
SELECT orders.id, 'pay_refund_rejected_001', 5000, 'SUCCESS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM orders
WHERE orders.order_number = 'ORD-REFUND-REJECTED-001'
  AND NOT EXISTS (SELECT 1 FROM payments WHERE order_id = orders.id);

INSERT INTO refunds (payment_id, amount, status, initiator, reason_type, reason, reviewed_by, reviewed_at, rejection_reason, created_at, updated_at)
SELECT payments.id, 3000, 'REJECTED', 'CUSTOMER', 'CUSTOMER_REQUEST', '테스트 거절 환불', admin.id, CURRENT_TIMESTAMP, '테스트 거절 사유', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM payments
JOIN users admin ON admin.email = 'refund-admin@onion.store'
WHERE payments.portone_payment_id = 'pay_refund_rejected_001'
  AND NOT EXISTS (SELECT 1 FROM refunds WHERE reason = '테스트 거절 환불');

INSERT INTO refund_items (refund_id, order_item_id, quantity)
SELECT refunds.id, order_items.id, 1
FROM refunds
JOIN payments ON payments.id = refunds.payment_id
JOIN orders ON orders.id = payments.order_id
JOIN order_items ON order_items.order_id = orders.id
WHERE refunds.reason = '테스트 거절 환불'
  AND order_items.product_name = '환불 테스트 양파즙 C'
  AND NOT EXISTS (
      SELECT 1 FROM refund_items
      WHERE refund_id = refunds.id AND order_item_id = order_items.id
  );

INSERT INTO orders (order_number, user_id, total_price, status, created_at, updated_at)
SELECT 'ORD-REFUND-FAILED-001', user.id, 5000, 'PAID', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM users user
WHERE user.email = 'refund-customer@onion.store'
  AND NOT EXISTS (SELECT 1 FROM orders WHERE order_number = 'ORD-REFUND-FAILED-001');

INSERT INTO order_items (order_id, product_id, product_name, product_price, quantity, created_at)
SELECT orders.id, products.id, products.name, products.price, 2, CURRENT_TIMESTAMP
FROM orders
JOIN products ON products.name = '환불 테스트 양파즙 B'
WHERE orders.order_number = 'ORD-REFUND-FAILED-001'
  AND NOT EXISTS (
      SELECT 1 FROM order_items
      WHERE order_id = orders.id AND product_id = products.id
  );

INSERT INTO order_items (order_id, product_id, product_name, product_price, quantity, created_at)
SELECT orders.id, products.id, products.name, products.price, 1, CURRENT_TIMESTAMP
FROM orders
JOIN products ON products.name = '환불 테스트 양파즙 A'
WHERE orders.order_number = 'ORD-REFUND-FAILED-001'
  AND NOT EXISTS (
      SELECT 1 FROM order_items
      WHERE order_id = orders.id AND product_id = products.id
  );

INSERT INTO payments (order_id, portone_payment_id, amount, status, paid_at, created_at, updated_at)
SELECT orders.id, 'pay_refund_failed_001', 5000, 'SUCCESS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM orders
WHERE orders.order_number = 'ORD-REFUND-FAILED-001'
  AND NOT EXISTS (SELECT 1 FROM payments WHERE order_id = orders.id);

INSERT INTO refunds (payment_id, amount, status, initiator, reason_type, reason, portone_cancellation_id, requested_cancellable_amount, created_at, updated_at)
SELECT payments.id, 2000, 'FAILED', 'CUSTOMER', 'CUSTOMER_REQUEST', '테스트 실패 환불', 'cancel_refund_failed_001', 5000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM payments
WHERE payments.portone_payment_id = 'pay_refund_failed_001'
  AND NOT EXISTS (SELECT 1 FROM refunds WHERE reason = '테스트 실패 환불');

INSERT INTO refund_items (refund_id, order_item_id, quantity)
SELECT refunds.id, order_items.id, 1
FROM refunds
JOIN payments ON payments.id = refunds.payment_id
JOIN orders ON orders.id = payments.order_id
JOIN order_items ON order_items.order_id = orders.id
WHERE refunds.reason = '테스트 실패 환불'
  AND order_items.product_name = '환불 테스트 양파즙 B'
  AND NOT EXISTS (
      SELECT 1 FROM refund_items
      WHERE refund_id = refunds.id AND order_item_id = order_items.id
  );

-- 5. 다른 고객 주문: 환불 상세·요청의 소유권 검증 테스트
INSERT INTO orders (order_number, user_id, total_price, status, created_at, updated_at)
SELECT 'ORD-REFUND-OTHER-001', user.id, 4000, 'PAID', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM users user
WHERE user.email = 'other-customer@onion.store'
  AND NOT EXISTS (SELECT 1 FROM orders WHERE order_number = 'ORD-REFUND-OTHER-001');

INSERT INTO order_items (order_id, product_id, product_name, product_price, quantity, created_at)
SELECT orders.id, products.id, products.name, products.price, 2, CURRENT_TIMESTAMP
FROM orders
JOIN products ON products.name = '환불 테스트 양파즙 A'
WHERE orders.order_number = 'ORD-REFUND-OTHER-001'
  AND NOT EXISTS (
      SELECT 1 FROM order_items
      WHERE order_id = orders.id AND product_id = products.id
  );

INSERT INTO order_items (order_id, product_id, product_name, product_price, quantity, created_at)
SELECT orders.id, products.id, products.name, products.price, 1, CURRENT_TIMESTAMP
FROM orders
JOIN products ON products.name = '환불 테스트 양파즙 B'
WHERE orders.order_number = 'ORD-REFUND-OTHER-001'
  AND NOT EXISTS (
      SELECT 1 FROM order_items
      WHERE order_id = orders.id AND product_id = products.id
  );

INSERT INTO payments (order_id, portone_payment_id, amount, status, paid_at, created_at, updated_at)
SELECT orders.id, 'pay_refund_other_001', 4000, 'SUCCESS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM orders
WHERE orders.order_number = 'ORD-REFUND-OTHER-001'
  AND NOT EXISTS (SELECT 1 FROM payments WHERE order_id = orders.id);

-- 다른 고객의 환불 이력: 고객 목록·상세 소유권 검증 테스트
INSERT INTO refunds (payment_id, amount, status, initiator, reason_type, reason, created_at, updated_at)
SELECT payments.id, 1000, 'PENDING_APPROVAL', 'CUSTOMER', 'CUSTOMER_REQUEST', '다른 고객 환불 요청', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM payments
WHERE payments.portone_payment_id = 'pay_refund_other_001'
  AND NOT EXISTS (SELECT 1 FROM refunds WHERE reason = '다른 고객 환불 요청');

INSERT INTO refund_items (refund_id, order_item_id, quantity)
SELECT refunds.id, order_items.id, 1
FROM refunds
JOIN payments ON payments.id = refunds.payment_id
JOIN orders ON orders.id = payments.order_id
JOIN order_items ON order_items.order_id = orders.id
WHERE refunds.reason = '다른 고객 환불 요청'
  AND order_items.product_name = '환불 테스트 양파즙 A'
  AND NOT EXISTS (
      SELECT 1 FROM refund_items
      WHERE refund_id = refunds.id AND order_item_id = order_items.id
  );
