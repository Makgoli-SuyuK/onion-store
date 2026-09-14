package com.example.onionstore.infra.webhook;

import com.example.onionstore.domain.payment.facade.PaymentFacade;
import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import io.portone.sdk.server.webhook.Webhook;
import io.portone.sdk.server.webhook.WebhookTransactionFailed;
import io.portone.sdk.server.webhook.WebhookTransactionPaid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentWebhookHandler {

    private final PaymentFacade paymentFacade;
    private final WebhookEventService webhookEventService;

    public void handle(String webhookId, Webhook webhook, String Payload) {
        String eventType = webhook.getClass().getSimpleName();
        String portonePaymentId = extractSupportedPortonePaymentId(webhook);

        WebhookEvent event = webhookEventService.registerOrGet(
                webhookId, eventType, portonePaymentId, Payload);

        WebhookClaimResult claimResult = webhookEventService.claimProcessing(event.getId());
        if (claimResult == WebhookClaimResult.ALREADY_COMPLETED) {
            return;
        }
        // 선점하지 못한 요청은 처리 중인 이벤트를 FAILED로 변경해서는 안 된다.
        if (claimResult == WebhookClaimResult.ALREADY_PROCESSING) {
            throw new BusinessException(ErrorCode.WEBHOOK_ALREADY_PROCESSING);
        }

        try {
            if (portonePaymentId == null) {
                log.debug("[Webhook] ignored unsupported event. webhookId={}, eventType={}", webhookId, eventType);

                webhookEventService.markIgnored(event.getId(), "처리대상 아님");
                return;
            }

            if (webhook instanceof WebhookTransactionPaid) {
                paymentFacade.confirmPaymentFromWebhook(portonePaymentId);
                webhookEventService.markProcessed(event.getId());
                return;
            }

            if (webhook instanceof WebhookTransactionFailed) {
                if (paymentFacade.synchronizePaymentFailureFromWebhook(portonePaymentId)) {
                    webhookEventService.markProcessed(event.getId());
                } else {
                    log.info("[Webhook] ignored failure event due to current gateway status. " + "webhookId={}, portonePaymentId={}", webhookId, portonePaymentId);

                    webhookEventService.markIgnored(event.getId(), "PortOne 실제 상태가 FAILED가 아님");
                }
            }

        } catch (RuntimeException exception) {
            log.error("[Webhook] processing failed. webhookId={}, eventId={}, " + "eventType={}, portonePaymentId={}", webhookId, event.getId(), eventType, portonePaymentId, exception);

            // 처리 실패 이력은 별도 트랜잭션으로 남기고,
            // 예외는 다시 전파해 웹훅 요청을 정상 처리로 끝내지 않는다.
            webhookEventService.markFailed(event.getId(), exception.getMessage());
            throw exception;
        }
    }

    private String extractSupportedPortonePaymentId(Webhook webhook) {
        if (webhook instanceof WebhookTransactionPaid paid) {
            return paid.getData().getPaymentId();
        }

        if (webhook instanceof WebhookTransactionFailed failed) {
            return failed.getData().getPaymentId();
        }
        return null;
    }
}