package com.da2jobu.notificationservice.interfaces.controller;

import com.da2jobu.notificationservice.application.service.NotificationService;
import com.da2jobu.notificationservice.infrastructure.client.dto.UserRole;
import com.da2jobu.notificationservice.infrastructure.client.dto.UserStatus;
import com.da2jobu.notificationservice.interfaces.dto.MessageSendAllRequest;
import com.da2jobu.notificationservice.interfaces.dto.MessageSendRequest;
import com.da2jobu.notificationservice.interfaces.dto.MessageSendResponse;
import com.da2jobu.notificationservice.interfaces.dto.SlackMessageCursorResponse;
import common.dto.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/users")
    public ResponseEntity<CommonResponse<MessageSendResponse>> sendMessage(
            @RequestBody MessageSendRequest request
    ) {
        return CommonResponse.ok(notificationService.sendMessage(request));
    }

    /**
     * 슬랙 메시지 커서 기반 조회
     * @param cursor 이전 조회의 마지막 createdAt (null이면 최신부터 조회)
     * @param size   한 번에 조회할 개수 (기본 20)
     */
    @GetMapping
    public ResponseEntity<CommonResponse<SlackMessageCursorResponse>> getMessages(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
            LocalDateTime cursor,
            @RequestParam(defaultValue = "10") int size
    ) {
        return CommonResponse.ok(notificationService.getMessages(cursor, size));
    }

    /**
     * 슬랙 메시지 단건 삭제
     */
    @DeleteMapping("/{messageId}")
    public ResponseEntity<CommonResponse<?>> deleteMessage(@PathVariable String messageId) {
        notificationService.deleteMessage(messageId);
        return CommonResponse.ok("메시지 삭제가 완료되었습니다.");
    }
}

