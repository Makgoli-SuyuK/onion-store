package com.example.onionstore.infra.webhook;

import com.example.onionstore.infra.config.PortOneProperties;
import io.portone.sdk.server.errors.WebhookVerificationException;
import io.portone.sdk.server.webhook.Webhook;
import io.portone.sdk.server.webhook.WebhookVerifier;
import org.springframework.stereotype.Component;

@Component
public class PortOneWebhookVerifier {

    private final WebhookVerifier webhookVerifier;

    public PortOneWebhookVerifier(PortOneProperties properties) {
        this.webhookVerifier = new WebhookVerifier(properties.getWebhookSecret());
    }

    public Webhook verify(String body, String webhookId, String signature, String timestamp) throws WebhookVerificationException {
        return webhookVerifier.verify(body, webhookId, signature, timestamp);

    }
}