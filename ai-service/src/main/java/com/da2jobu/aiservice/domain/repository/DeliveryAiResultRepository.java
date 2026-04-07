package com.da2jobu.aiservice.domain.repository;

import com.da2jobu.aiservice.domain.model.DeliveryAiResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface DeliveryAiResultRepository extends JpaRepository<DeliveryAiResult, UUID> {

    /**
     * pgvector 코사인 유사도 기반 유사 배송 결과 검색 (RAG용)
     * 가장 유사한 과거 배송 결과 상위 k개 반환
     */
    @Query(value = """
            SELECT * FROM ai_output.p_delivery_ai_result
            WHERE embedding IS NOT NULL
            ORDER BY embedding <=> CAST(:embedding AS vector)
            LIMIT :k
            """, nativeQuery = true)
    List<DeliveryAiResult> findSimilarResults(
            @Param("embedding") String embedding,
            @Param("k") int k
    );
}
