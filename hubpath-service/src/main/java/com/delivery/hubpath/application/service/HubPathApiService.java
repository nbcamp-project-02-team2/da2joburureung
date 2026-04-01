package com.delivery.hubpath.application.service;

import com.delivery.hubpath.application.dto.CreateHubPathCommand;
import com.delivery.hubpath.domain.model.HubPath;
import com.delivery.hubpath.domain.repository.HubPathRepository;
import com.delivery.hubpath.infrastructure.client.HubClient;
import com.delivery.hubpath.infrastructure.client.HubResponse;
import com.delivery.hubpath.interfaces.dto.response.HubPathResponse;
import common.client.KakaoAddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HubPathApiService {

    private final HubClient hubClient;
    private final HubPathRepository hubPathRepository;
    private final KakaoAddressService kakaoAddressService;

    @CacheEvict(cacheNames = "hubPages", allEntries = true)
    @Transactional
    public HubPathResponse createHubPath(CreateHubPathCommand command) {

        HubResponse departHub = hubClient.getHubByName(command.departHubName()).getData();
        if (departHub == null) {
            throw new IllegalArgumentException("출발 허브 정보를 찾을 수 없습니다. ID: " + command.departHubName());
        }
        HubResponse arriveHub = hubClient.getHubByName(command.arriveHubName()).getData();
        if (arriveHub == null) {
            throw new IllegalArgumentException("도착 허브 정보를 찾을 수 없습니다. ID: " + command.arriveHubName());
        }

        List<HubResponse> allHubs = hubClient.getAllHubs().getData().getContent();

        HubPath hubPath = HubPath.createPath(departHub, arriveHub, allHubs, kakaoAddressService);
        HubPath savedPath = hubPathRepository.save(hubPath);

        return HubPathResponse.from(savedPath);
    }
}
