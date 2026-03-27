package com.delivery.hub.interfaces.controller;

import com.delivery.hub.application.service.hubApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/hub")
public class hubController {

    private final hubApiService hubApiService;

}
