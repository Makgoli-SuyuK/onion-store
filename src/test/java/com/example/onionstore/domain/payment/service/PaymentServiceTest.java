package com.example.onionstore.domain.payment.service;

import com.example.onionstore.domain.payment.repository.PaymentRepository;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void 결제정보가_없는_주문조회() {
        // given
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.empty());

        // when & then
        BusinessException exception = assertThrows(BusinessException.class, () -> paymentService.getPaymentByOrderId(1L));
        assertEquals(ErrorCode.PAYMENT_NOT_FOUND, exception.getErrorCode());
    }
}