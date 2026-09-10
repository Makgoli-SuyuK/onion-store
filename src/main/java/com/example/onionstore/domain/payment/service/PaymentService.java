package com.example.onionstore.domain.payment.service;

import com.example.onionstore.domain.order.entity.Order;
import com.example.onionstore.domain.payment.dto.CreatePaymentResponse;
import com.example.onionstore.domain.payment.dto.GetPaymentInfoResponse;
import com.example.onionstore.domain.payment.dto.PaymentStateChangeResponse;
import com.example.onionstore.domain.payment.entity.Payment;
import com.example.onionstore.domain.payment.repository.PaymentRepository;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    // 주문 생성 흐름에서 저장된 주문과 서버계산 금액을 전달.
    @Transactional
    public CreatePaymentResponse createPayment(Order order, long amount) {
        Payment payment = new Payment(order, amount);
        Payment createdPayment = paymentRepository.save(payment);
        return CreatePaymentResponse.from(createdPayment);
    }

    @Transactional(readOnly = true)
    public GetPaymentInfoResponse getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId).orElseThrow(
                () -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND)
        );

        return GetPaymentInfoResponse.from(payment);
    }

    // 포트원 성공 결과 검증 결과를 내부 호출자가 사용
    @Transactional
    public PaymentStateChangeResponse applySuccess(Long orderId) {
        Payment payment = findPaymentForUpdate(orderId);
        boolean changed = payment.markAsSuccess();
        return new PaymentStateChangeResponse(changed, GetPaymentInfoResponse.from(payment));
    }

    // 결제 실패 확인 된 경우 사용
    @Transactional
    public PaymentStateChangeResponse applyFailure(Long orderId) {
        Payment payment = findPaymentForUpdate(orderId);
        boolean changed = payment.markAsFailed();
        return new PaymentStateChangeResponse(changed, GetPaymentInfoResponse.from(payment));
    }

    // 취소 가능 여부와 필요한 PG취소 완료 확인후에 사용한다
    @Transactional
    public PaymentStateChangeResponse applyCancellation(Long orderId) {
        Payment payment = findPaymentForUpdate(orderId);
        boolean changed = payment.markAsCancelled();
        return new PaymentStateChangeResponse(changed, GetPaymentInfoResponse.from(payment));
    }

    private Payment findPaymentForUpdate(Long orderId) {
        return paymentRepository.findByOrderIdForUpdate(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
    }

}
