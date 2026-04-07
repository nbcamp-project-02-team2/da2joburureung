package com.da2jobu.notificationservice.domain.repository;

import com.da2jobu.notificationservice.domain.model.SlackMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface SlackMessageRepository extends JpaRepository<SlackMessage, UUID> {

    /**
     * createdAt 기준 커서 페이지네이션
     * cursor가 null이면 최신 메시지부터 조회
     */
    @Query("""
            SELECT s FROM SlackMessage s
            WHERE (:cursor IS NULL OR s.createdAt < :cursor)
            ORDER BY s.createdAt DESC
            """)
    List<SlackMessage> findByCursor(
            @Param("cursor") LocalDateTime cursor,
            Pageable pageable
    );
}
