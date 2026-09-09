package com.example.onionstore.domain.payment.service;

import com.example.onionstore.domain.payment.dto.GetPaymentInfoResponse;
import com.example.onionstore.domain.payment.entity.Payment;
import com.example.onionstore.domain.payment.repository.PaymentRepository;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;


    public GetPaymentInfoResponse getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId).orElseThrow(
                () -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND)
        );

        return GetPaymentInfoResponse.from(payment);
    }
}
