package com.da2jobu.notificationservice.infrastructure.slack;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
public class SlackMessageSendTest {

    @Autowired
    com.da2jobu.notificationservice.domain.model.MessageSend messageSend;

    @Value("${slack.token}")
    String token;

    @Test
    void messageSendTest() {
        System.out.println("====전송");
        System.out.println(token);
        messageSend.send(List.of("U0ANF5R1YR4"), "테스트 메세지, 잘 전송이 되나요?");
        System.out.println("====완료");
    }
}