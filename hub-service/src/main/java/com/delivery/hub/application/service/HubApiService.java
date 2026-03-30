package com.delivery.hub.application.service;

import com.delivery.hub.application.dto.CreateHubCommand;
import com.delivery.hub.application.dto.SearchHubCommand;
import com.delivery.hub.domain.model.Hub;
import com.delivery.hub.domain.repository.HubRepository;
import com.delivery.hub.infrastructure.client.KakaoAddressService;
import com.delivery.hub.infrastructure.config.Redis.RestPage;
import com.delivery.hub.interfaces.dto.Respone.HubResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class HubApiService {

    private final HubRepository hubrepository;
    private final KakaoAddressService kakaoaddressservice;

    // 허브 저장
    @CacheEvict(cacheNames = "hubPages", allEntries = true)
    @Transactional
    public HubResponse createHub(CreateHubCommand command) {
        if (hubrepository.existsByHubName(command.hub_name())) {
            throw new IllegalArgumentException("이미 존재하는 허브 이름입니다: " + command.hub_name());
        }

        KakaoAddressService.GeoPoint geoPoint = kakaoaddressservice.getGeoPoint(command.address());

        Hub hub = Hub.createHub(
                command.hub_name(), command.address(), geoPoint.latitude(), geoPoint.longitude()
        );

        Hub save = hubrepository.save(hub);

        return HubResponse.from(save);
    }

    //허브 전체 조회 및 검색
    @Cacheable(cacheNames = "hubPages", key = "{#command.hub_name(), #command.address(), #pageable.pageNumber, #pageable.pageSize, #pageable.sort.toString()}")
    public RestPage<HubResponse> getHubs(SearchHubCommand command, Pageable pageable) {
        Page<HubResponse> result = hubrepository.searchHubs(command, pageable);

        return new RestPage<>(result);
    }
}
