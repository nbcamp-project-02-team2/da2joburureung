package com.da2jobu.notificationservice.application.service.impl;

import com.da2jobu.notificationservice.application.service.NotificationService;
import com.da2jobu.notificationservice.domain.model.MessageSend;
import com.da2jobu.notificationservice.domain.model.SlackMessage;
import com.da2jobu.notificationservice.domain.repository.SlackMessageRepository;
import com.da2jobu.notificationservice.infrastructure.client.UserClient;
import com.da2jobu.notificationservice.interfaces.dto.MessageSendRequest;
import com.da2jobu.notificationservice.interfaces.dto.MessageSendResponse;
import com.da2jobu.notificationservice.interfaces.dto.SlackMessageCursorResponse;
import common.exception.CustomException;
import common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final MessageSend messageSend;
    private final SlackMessageRepository slackMessageRepository;
    private final UserClient userClient;

    @Override
    public MessageSendResponse sendMessage(MessageSendRequest request) {
        messageSend.send(request.ids(), request.message());
        return new MessageSendResponse(request.ids(), request.message());
    }

    @Override
    @Transactional(readOnly = true)
    public SlackMessageCursorResponse getMessages(LocalDateTime cursor, int size) {
        int fetchSize = size + 1; // 다음 페이지 존재 여부 확인용
        List<SlackMessage> results = slackMessageRepository.findByCursor(cursor, PageRequest.of(0, fetchSize));

        boolean hasNext = results.size() > size;
        List<SlackMessage> messages = hasNext ? results.subList(0, size) : results;

        LocalDateTime nextCursor = hasNext ? messages.get(messages.size() - 1).getCreatedAt() : null;

        List<SlackMessageCursorResponse.SlackMessageDto> dtos = messages.stream()
                .map(m -> new SlackMessageCursorResponse.SlackMessageDto(
                        m.getSlackMessageId().toString(),
                        m.getRecipientId().toString(),
                        m.getMessage(),
                        m.getMessageType().name(),
                        m.getMessageStatus().name(),
                        m.getRetryCount(),
                        m.getSucceededAt(),
                        m.getCreatedAt()
                ))
                .toList();

        return new SlackMessageCursorResponse(dtos, nextCursor, hasNext);
    }

    @Override
    @Transactional
    public void deleteMessage(String messageId) {
        UUID id = UUID.fromString(messageId);
        SlackMessage message = slackMessageRepository.findById(id).orElseThrow(
                () -> new CustomException(ErrorCode.NOTIFICATION_NOT_DELETED)
        );
        message.softDelete("MASTER");
    }
}
