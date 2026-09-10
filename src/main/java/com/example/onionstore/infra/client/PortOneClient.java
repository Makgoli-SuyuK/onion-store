package com.example.onionstore.infra.client;

import com.example.onionstore.domain.payment.port.PaymentGateway;
import com.example.onionstore.domain.payment.port.PaymentGatewayResponse;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import com.example.onionstore.infra.dto.PortOnePaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
public class PortOneClient implements PaymentGateway {

    private final RestClient portoneRestClient;

    @Override
    public PaymentGatewayResponse getPayment(String portonePaymentId) {
        try {
            PortOnePaymentResponse response = portoneRestClient.get()
                    .uri("/payments/{portonePaymentId}", portonePaymentId)
                    .retrieve()
                    .body(PortOnePaymentResponse.class);

            if (response == null) {
                throw new BusinessException(ErrorCode.PAYMENT_GATEWAY_ERROR);
            }
            return new PaymentGatewayResponse(
                    response.id(),
                    response.amount() == null ? null : response.amount().total(),
                    "PAID".equals(response.status())
            );
        } catch (RestClientException exception) {
            throw new BusinessException(ErrorCode.PAYMENT_GATEWAY_ERROR);
        }
    }
}
