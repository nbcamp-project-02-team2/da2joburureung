package com.da2jobu.notificationservice.domain.model;

import java.util.List;

public interface MessageSend {
    void send(List<String> ids, String message);
}
