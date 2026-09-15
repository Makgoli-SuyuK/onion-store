package com.example.onionstore.domain.refund.entity;

public enum RefundStatus {

    PENDING_APPROVAL {
        @Override
        public boolean canTransitTo(RefundStatus target) {
            return target == REQUESTED || target == REJECTED;
        }
    },

    REQUESTED {
        @Override
        public boolean canTransitTo(RefundStatus target) {
            return target == COMPLETED || target == FAILED;
        }
    },

    COMPLETED {
        @Override
        public boolean canTransitTo(RefundStatus target) {
            return false;
        }
    },

    REJECTED {
        @Override
        public boolean canTransitTo(RefundStatus target) {
            return false;
        }
    },

    FAILED {
        @Override
        public boolean canTransitTo(RefundStatus target) {
            return false;
        }
    };

    public abstract boolean canTransitTo(RefundStatus target);
}