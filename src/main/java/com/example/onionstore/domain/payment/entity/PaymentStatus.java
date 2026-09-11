package com.example.onionstore.domain.payment.entity;

public enum PaymentStatus {
    READY {
        @Override
        public boolean canTransitTo(PaymentStatus target) {
            return target == SUCCESS || target == FAILED || target == CANCELLED;

        }
    },
    SUCCESS {
        @Override
        public boolean canTransitTo(PaymentStatus target) {
            return false;
        }
    },
    FAILED {
        public boolean canTransitTo(PaymentStatus target) {
            return target == CANCELLED;
        }
    },
    CANCELLED {
        @Override
        public boolean canTransitTo(PaymentStatus target) {
            return false;
        }
    };

    public abstract boolean canTransitTo(PaymentStatus target);
}
