package com.example.onionstore.infra.webhook;

import com.example.onionstore.global.exception.BusinessException;
import com.example.onionstore.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WebhookEventService {

    private final WebhookEventRepository webhookEventRepository;

    // 중복 웹훅이면 기존 이벤트를 반환하고, 신규 이벤트면 저장한다.
    public WebhookEvent registerOrGet(String webhookId, String type, String portonePaymentId, String payload) {
        return webhookEventRepository.findByWebhookId(webhookId)
                .orElseGet(() -> insertOrGetExisting(webhookId, type, portonePaymentId, payload));

    }

    // 동시 중복 INSERT 충돌 시 기존 웹훅 이벤트를 다시 조회해 반환한다.
    private WebhookEvent insertOrGetExisting(String webhookId, String eventType, String portonePaymentId, String payload) {

        try {
            return webhookEventRepository.saveAndFlush(
                    new WebhookEvent(webhookId, eventType, portonePaymentId, payload)
            );
        } catch (DataIntegrityViolationException e) {
            return webhookEventRepository.findByWebhookId(webhookId).orElseThrow(() -> e);
        }

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean markProcessed(Long eventId) {
        return findForUpdate(eventId).markProcessed();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean markFailed(Long eventId, String message) {
        return findForUpdate(eventId).markFailed(message);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean markIgnored(Long eventId, String message) {
        return findForUpdate(eventId).markIgnored(message);
    }

    private WebhookEvent findForUpdate(Long eventId) {
        return webhookEventRepository.findByIdForUpdate(eventId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WEBHOOK_EVENT_NOT_FOUND));
    }
}
