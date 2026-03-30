package com.delivery.hub.application.service;

import com.delivery.hub.application.dto.CreateHubCommand;
import com.delivery.hub.application.dto.SearchHubCommand;
import com.delivery.hub.domain.model.Hub;
import com.delivery.hub.domain.repository.HubRepository;
import com.delivery.hub.infrastructure.client.KakaoAddressService;
import com.delivery.hub.interfaces.dto.Respone.HubResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HubApiService {

    private final HubRepository hubrepository;
    private final KakaoAddressService kakaoaddressservice;

    // 허브 저장
    @Transactional
    public HubResponse createHub(CreateHubCommand command) {

        KakaoAddressService.GeoPoint geoPoint = kakaoaddressservice.getGeoPoint(command.address());

        Hub hub = Hub.createHub(
                command.hub_name(), command.address(), geoPoint.latitude(), geoPoint.longitude()
        );

        Hub save = hubrepository.save(hub);

        return HubResponse.from(save);
    }

    //허브 전체 조회 및 검색
    public Page<HubResponse> getHubs(SearchHubCommand command, Pageable pageable) {
        return hubrepository.searchHubs(command, pageable);
    }
}
