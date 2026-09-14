package com.example.onionstore.infra.webhook;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface WebhookEventRepository extends JpaRepository<WebhookEvent, Long> {

    Optional<WebhookEvent> findByWebhookId(String webhookId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT event FROM WebhookEvent event WHERE event.id = :eventId")
    Optional<WebhookEvent> findByIdForUpdate(@Param("eventId") Long eventId);
}
