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
