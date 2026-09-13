package com.example.onionstore.infra.webhook;

import com.example.onionstore.domain.payment.facade.PaymentFacade;
import com.example.onionstore.domain.payment.port.PaymentGateway;
import com.example.onionstore.domain.payment.service.PaymentCommandService;
import com.example.onionstore.domain.payment.service.PaymentService;
import io.portone.sdk.server.webhook.Webhook;
import io.portone.sdk.server.webhook.WebhookTransactionFailed;
import io.portone.sdk.server.webhook.WebhookTransactionPaid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentWebhookHandler {

    private final PaymentFacade paymentFacade;
    private final PaymentGateway paymentGateway;
    private final PaymentService paymentService;
    private final PaymentCommandService paymentCommandService;
    private final WebhookEventService webhookEventService;

    public void handle(String webhookId, Webhook webhook, String rawPayload) {
        String type = webhook.getClass().getSimpleName();
        String portonePaymentId = (webhook);
        WebhookEvent webhookEvent = webhookEventService.registerOrGet()
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
    private boolean synchronizeFailure(String portonePaymentId) {
        Pay
    }

}
