package com.da2jobu.notificationservice.interfaces.dto;

import java.util.List;

public record MessageSendResponse(
        List<String> ids,
        String message
) {
}
