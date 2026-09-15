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
