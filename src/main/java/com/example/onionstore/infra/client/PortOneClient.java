package com.example.onionstore.infra.client;

import com.example.onionstore.domain.payment.port.PaymentGateway;
import com.example.onionstore.domain.payment.port.PaymentGatewayResponse;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import com.example.onionstore.infra.config.PortOneProperties;
import com.example.onionstore.infra.dto.PortOnePaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
@RequiredArgsConstructor
@Slf4j
public class PortOneClient implements PaymentGateway {

    private final RestClient portoneRestClient;
    private final PortOneProperties portoneProperties;

    @Override
    public PaymentGatewayResponse getPayment(String portonePaymentId) {
        try {
            PortOnePaymentResponse response = portoneRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/payments/{portonePaymentId}")
                            .queryParam("storeId", portoneProperties.getStoreId())
                            .build(portonePaymentId))
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
        } catch (RestClientResponseException exception) {
            log.warn("PortOne 결제 조회 실패: paymentId={}, httpStatus={}, responseBody={}",
                    portonePaymentId,
                    exception.getStatusCode().value(),
                    exception.getResponseBodyAsString());
            throw new BusinessException(ErrorCode.PAYMENT_GATEWAY_ERROR);
        } catch (RestClientException exception) {
            log.warn("PortOne 결제 조회 통신 실패: paymentId={}, errorType={}",
                    portonePaymentId, exception.getClass().getSimpleName());
            throw new BusinessException(ErrorCode.PAYMENT_GATEWAY_ERROR);
        }
    }

}
