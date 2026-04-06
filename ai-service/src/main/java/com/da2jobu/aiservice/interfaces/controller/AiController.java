package com.da2jobu.aiservice.interfaces.controller;

import com.da2jobu.aiservice.application.service.AiDeliveryService;
import com.da2jobu.aiservice.interfaces.controller.dto.AiResponse;
import common.dto.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
public class AiController {
    private final AiDeliveryService aiDeliveryService;

    @GetMapping
    public ResponseEntity<CommonResponse<AiResponse>> getAiHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return CommonResponse.ok("AI 배송 이력 조회에 성공했습니다.", aiDeliveryService.getAiHistory(page, size));
    }
}
