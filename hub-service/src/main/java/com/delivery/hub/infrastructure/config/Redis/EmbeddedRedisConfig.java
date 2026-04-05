package com.delivery.hub.infrastructure.config.Redis;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import redis.embedded.RedisServer;

@Slf4j
@Configuration
public class EmbeddedRedisConfig {

    @Value("${SPRING_DATA_REDIS_PORT}")
    private int redisPort;

    private RedisServer redisServer;

    @PostConstruct
    public void startRedis() {
        try {
            redisServer = new RedisServer(redisPort);
            redisServer.start();
            log.info("내장 Redis 시작 (Port: {})", redisPort);
        } catch (Exception e) {
            log.error("내장 Redis 시작 실패: {}", e.getMessage());
        }
    }

    @PreDestroy
    public void stopRedis() {
        if (redisServer != null) {
            try {
                redisServer.stop();
                log.info("내장 Redis 종료 완료");
            } catch (Exception e) {
                log.warn("내장 Redis 종료 중 예외 발생: {}", e.getMessage());
            }
        }
    }
}