package com.delivery.hubpath.infrastructure.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Configuration;
import redis.embedded.RedisServer;

import java.io.IOException;

@Configuration
public class EmbeddedRedisConfig {

    private RedisServer redisServer;

    @PostConstruct
    public void startRedis() {
        try {
            redisServer = new RedisServer(6379);
            redisServer.start();
            System.out.println("내장 Redis 서버가 6379 포트에서 시작되었습니다.");
        } catch (Exception e) {
            System.out.println(" 내장 Redis 시작 실패: " + e.getMessage());
        }
    }

    @PreDestroy
    public void stopRedis() throws IOException {
        if (redisServer != null) {
            redisServer.stop();
        }
    }
}