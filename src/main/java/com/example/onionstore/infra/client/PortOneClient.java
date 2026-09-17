package com.example.onionstore.infra.client;

import com.example.onionstore.domain.payment.port.GatewayCancellationStatus;
import com.example.onionstore.domain.payment.port.GatewayPaymentStatus;
import com.example.onionstore.domain.payment.port.PaymentCancellationResponse;
import com.example.onionstore.domain.payment.port.PaymentGateway;
import com.example.onionstore.domain.payment.port.PaymentGatewayResponse;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import com.example.onionstore.infra.config.PortOneProperties;
import com.example.onionstore.infra.dto.PortOneCancelRequest;
import com.example.onionstore.infra.dto.PortOneCancelResponse;
import com.example.onionstore.infra.dto.PortOnePaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PortOneClient implements PaymentGateway {

    private final RestClient portoneRestClient;
    private final PortOneProperties portoneProperties;

    @Override
    public PaymentGatewayResponse getPayment(String portonePaymentId) {
        try {
            PortOnePaymentResponse response = requestPayment(portonePaymentId);
            return new PaymentGatewayResponse(
                    response.id(),
                    toGatewayPaymentStatus(response.status()),
                    response.amount() == null ? null : response.amount().total()
            );
        } catch (RestClientResponseException exception) {
            if (isPaymentNotFound(exception)) {
                return new PaymentGatewayResponse(portonePaymentId, GatewayPaymentStatus.NOT_FOUND, null);
            }
            log.warn(
                    "PortOne 결제 조회 실패: portonePaymentId={}, httpStatus={}, responseBody={}",
                    portonePaymentId,
                    exception.getStatusCode().value(),
                    exception.getResponseBodyAsString());
            throw new BusinessException(ErrorCode.PAYMENT_GATEWAY_ERROR);
        } catch (RestClientException exception) {
            log.warn(
                    "PortOne 결제 조회 통신 실패: portonePaymentId={}, errorType={}",
                    portonePaymentId, exception.getClass().getSimpleName());
            throw new BusinessException(ErrorCode.PAYMENT_GATEWAY_ERROR);
        }
    }

    @Override
    public void cancelPaymentForAmountMismatch(String portonePaymentId, String reason) {
        try {
            String idempotencyKey = UUID.nameUUIDFromBytes(
                    ("amount-mismatch-cancel:" + portonePaymentId).getBytes(StandardCharsets.UTF_8)
            ).toString();
            portoneRestClient.post()
                    .uri("/payments/{portonePaymentId}/cancel", portonePaymentId)
                    .header("Idempotency-Key", "\"" + idempotencyKey + "\"")
                    .body(new PortOneCancelRequest(reason, portoneProperties.getStoreId()))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException exception) {
            log.warn("PortOne 결제 취소 실패: portonePaymentId={}, httpStatus={}, responseBody={}",
                    portonePaymentId,
                    exception.getStatusCode().value(),
                    exception.getResponseBodyAsString());
            throw new BusinessException(ErrorCode.PAYMENT_CANCELLATION_FAILED);
        } catch (RestClientException exception) {
            log.warn("PortOne 결제 취소 통신 실패: portonePaymentId={}, errorType={}",
                    portonePaymentId, exception.getClass().getSimpleName());
            throw new BusinessException(ErrorCode.PAYMENT_CANCELLATION_FAILED);
        }
    }

    @Override
    public PaymentCancellationResponse requestPartialCancellation(
            String portonePaymentId,
            long amount,
            String reason
    ) {
        try {
            PortOneCancelResponse response = portoneRestClient.post()
                    .uri("/payments/{portonePaymentId}/cancel", portonePaymentId)
                    .body(new PortOneCancelRequest(reason, portoneProperties.getStoreId(), amount))
                    .retrieve()
                    .body(PortOneCancelResponse.class);

            if (response == null || response.cancellation() == null
                    || response.cancellation().id() == null) {
                throw new BusinessException(ErrorCode.PAYMENT_CANCELLATION_FAILED);
            }

            log.info("PortOne 부분 취소 요청 완료: portonePaymentId={}, cancellationId={}, amount={}",
                    portonePaymentId, response.cancellation().id(), amount);
            return new PaymentCancellationResponse(
                    response.cancellation().id(),
                    GatewayCancellationStatus.from(response.cancellation().status())
            );
        } catch (RestClientResponseException exception) {
            log.warn("PortOne 부분 취소 실패: portonePaymentId={}, amount={}, httpStatus={}, responseBody={}",
                    portonePaymentId,
                    amount,
                    exception.getStatusCode().value(),
                    exception.getResponseBodyAsString());
            throw new BusinessException(ErrorCode.PAYMENT_CANCELLATION_FAILED);
        } catch (RestClientException exception) {
            log.warn("PortOne 부분 취소 통신 실패: portonePaymentId={}, amount={}, errorType={}",
                    portonePaymentId, amount, exception.getClass().getSimpleName());
            throw new BusinessException(ErrorCode.PAYMENT_CANCELLATION_FAILED);
        }
    }

    private PortOnePaymentResponse requestPayment(String portonePaymentId) {
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

        return response;
    }

    private boolean isPaymentNotFound(RestClientResponseException exception) {
        return exception.getStatusCode().value() == 404
                && exception.getResponseBodyAsString().contains("\"type\":\"PAYMENT_NOT_FOUND\"");
    }

    private GatewayPaymentStatus toGatewayPaymentStatus(String portoneStatus) {
        if (portoneStatus == null) {
            return GatewayPaymentStatus.UNKNOWN;
        }
        try {
            return GatewayPaymentStatus.valueOf(portoneStatus);
        } catch (IllegalArgumentException exception) {
            return GatewayPaymentStatus.UNKNOWN;
        }
    }
}
