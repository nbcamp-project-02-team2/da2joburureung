package com.da2jobu.notificationservice.interfaces.dto;

import java.time.LocalDateTime;
import java.util.List;

public record SlackMessageCursorResponse(
        List<SlackMessageDto> messages,
        LocalDateTime nextCursor,
        boolean hasNext
) {
    public record SlackMessageDto(
            String slackMessageId,
            String recipientId,
            String message,
            String messageType,
            String messageStatus,
            int retryCount,
            LocalDateTime succeededAt,
            LocalDateTime createdAt
    ) {}
}
