package com.da2jobu.infrastructure.messaging.outbox;

public enum OutboxStatus {
    PENDING,
    PUBLISHED,
    FAILED
}
