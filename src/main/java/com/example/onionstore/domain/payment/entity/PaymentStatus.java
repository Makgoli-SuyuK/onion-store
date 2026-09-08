package com.example.onionstore.domain.payment.entity;

public enum PaymentStatus {
    READY {
        @Override
        public boolean canTransitTo(PaymentStatus target) {
            return target == SUCCESS || target == FAILED;

        }
    },
    SUCCESS {
        @Override
        public boolean canTransitTo(PaymentStatus target) {
            return target == FAILED;
        }
    },
    FAILED {
        public boolean canTransitTo(PaymentStatus target) {
            return false;
        }
    };

    public abstract boolean canTransitTo(PaymentStatus target);
}