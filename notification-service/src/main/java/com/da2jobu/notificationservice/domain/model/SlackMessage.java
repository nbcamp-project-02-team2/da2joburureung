package com.da2jobu.notificationservice.domain.model;

import common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "p_slack_message")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class SlackMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID slackMessageId;
    @Column(name = "recipient_id", nullable = false)
    private UUID recipientId;
    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    private String message;
    @Enumerated(value = EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    private MessageType messageType;
    @Enumerated(value = EnumType.STRING)
    @Column(name = "message_status", nullable = false)
    private MessageStatus messageStatus;
    @Column(name = "retry_count", nullable = false)
    private int retryCount;
    @Column(name = "succeeded_at", nullable = true)
    private LocalDateTime succeededAt;

    private SlackMessage(UUID recipientId, String message, MessageType messageType, MessageStatus messageStatus){
        this.recipientId = recipientId;
        this.message = message;
        this.messageType = messageType;
        this.messageStatus = messageStatus;
        this.retryCount = 0;
    }

    public void retry(){
        this.retryCount++;
    }

    public void markSucceeded(){
        this.messageStatus = MessageStatus.SUCCESS;
        this.succeededAt = LocalDateTime.now();
    }

    public void markFailed(){
        this.messageStatus = MessageStatus.FAIL;
    }

    public void markFailed(int maxRetry) {
        this.retryCount++;
        // 재시도 횟수가 남아있으면 PENDING으로 되돌려 다음 폴링에서 재시도
        // 최대 재시도 초과 시 최종 FAILED 처리
        this.messageStatus = this.retryCount < maxRetry ? MessageStatus.PENDING : MessageStatus.FAIL;
    }

    public boolean isExhausted(int maxRetry) {
        return this.retryCount >= maxRetry;
    }

    public static SlackMessage create(UUID recipientId, String message, MessageType messageType, MessageStatus messageStatus ){
        return new SlackMessage(recipientId, message, messageType, messageStatus);
    }
}
