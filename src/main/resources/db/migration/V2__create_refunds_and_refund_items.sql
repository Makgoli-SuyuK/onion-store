CREATE TABLE refunds (
    id BIGINT NOT NULL AUTO_INCREMENT,
    payment_id BIGINT NOT NULL,
    amount BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    initiator VARCHAR(20) NOT NULL,
    reason_type VARCHAR(40) NOT NULL,
    reason VARCHAR(500) NOT NULL,
    reviewed_by BIGINT NULL,
    reviewed_at DATETIME(6) NULL,
    rejection_reason VARCHAR(500) NULL,
    portone_cancellation_id VARCHAR(255) NULL,
    requested_cancellable_amount BIGINT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_refunds_portone_cancellation_id UNIQUE (portone_cancellation_id),
    CONSTRAINT fk_refunds_payment
        FOREIGN KEY (payment_id) REFERENCES payments (id),
    CONSTRAINT fk_refunds_reviewed_by
        FOREIGN KEY (reviewed_by) REFERENCES users (id),
    INDEX idx_refunds_payment_id (payment_id),
    INDEX idx_refunds_status_created_at (status, created_at)
);

CREATE TABLE refund_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    refund_id BIGINT NOT NULL,
    order_item_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_refund_item UNIQUE (refund_id, order_item_id),
    CONSTRAINT fk_refund_items_refund
        FOREIGN KEY (refund_id) REFERENCES refunds (id),
    CONSTRAINT fk_refund_items_order_item
        FOREIGN KEY (order_item_id) REFERENCES order_items (id),
    INDEX idx_refund_items_order_item_id (order_item_id)
);
