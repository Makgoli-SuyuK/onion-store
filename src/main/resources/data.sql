INSERT INTO users (email, password, name, phone_number, role, status, created_at, updated_at)
SELECT 'admin@onion.store',
       '$2y$10$0sCj0rqVo3QYOz8M5RIj3OX4jKE726oKhqS8K475mc/xnxD7dXJ/G',
       '관리자', '01000000000', 'ADMIN', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@onion.store');
