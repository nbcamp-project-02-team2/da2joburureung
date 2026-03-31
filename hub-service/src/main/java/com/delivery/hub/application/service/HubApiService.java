package com.delivery.hub.application.service;

import com.delivery.hub.application.dto.CreateHubCommand;
import com.delivery.hub.application.dto.SearchHubCommand;
import com.delivery.hub.domain.model.Hub;
import com.delivery.hub.domain.repository.HubRepository;
import com.delivery.hub.infrastructure.client.KakaoAddressService;
import com.delivery.hub.infrastructure.config.Redis.RestPage;
import com.delivery.hub.interfaces.dto.Request.UpdateHubRequest;
import com.delivery.hub.interfaces.dto.Respone.HubResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

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
    @Cacheable(
            cacheNames = "hubPages",
            key = "{#command.hub_name(), #command.address(), #pageable.pageNumber, #pageable.pageSize, #pageable.sort}")
    public RestPage<HubResponse> getHubs(SearchHubCommand command, Pageable pageable) {
        Page<HubResponse> result = hubrepository.searchHubs(command, pageable);
        return new RestPage<>(result);
    }

    //특정 허브 상세 내용 조회
    @Cacheable(cacheNames = "hubDetails", key = "#hubId")
    public HubResponse getHub(@Valid UUID hubId) {
        Hub hub = hubrepository.findById(hubId)
                .orElseThrow(() -> new IllegalArgumentException("해당 허브를 찾을 수 없습니다. ID: " + hubId));

        return HubResponse.detailFrom(hub);
    }

    // 허브 내용 수정
    @Transactional
    @CacheEvict(cacheNames = "hubPages", allEntries = true)
    public HubResponse updateHub(UUID hubId, @Valid UpdateHubRequest request) {

        Hub hub = hubrepository.findById(hubId)
                .orElseThrow(() -> new IllegalArgumentException("해당 허브가 존재하지 않습니다."));
        if (request.hub_name() != null && !hub.getHub_name().equals(request.hub_name())) {
            if (hubrepository.existsByHubName(request.hub_name())) {
                throw new IllegalArgumentException("이미 사용 중인 이름입니다.");
            }
        }

        BigDecimal newLat = hub.getLatitude();
        BigDecimal newLon = hub.getLongitude();
        if (request.address() != null && !hub.getAddress().equals(request.address())) {
            KakaoAddressService.GeoPoint geoPoint = kakaoaddressservice.getGeoPoint(request.address());
            newLat = geoPoint.latitude();
            newLon = geoPoint.longitude();
        }

        hub.updateHub(
                request.hub_name() != null ? request.hub_name() : hub.getHub_name(),
                request.address() != null ? request.address() : hub.getAddress(),
                newLat,
                newLon,
                "master"
        );

        return HubResponse.detailFrom(hub);
    }

    // 허브 삭제
    @Transactional
    @CacheEvict(cacheNames = {"hubPages", "hubDetails"}, allEntries = true)
    public void deleteHub(UUID hubId) {

        Hub hub = hubrepository.findById(hubId)
                .orElseThrow(() -> new IllegalArgumentException("삭제하려는 허브가 존재하지 않습니다. ID: " + hubId));
        if (hub.isDeleted()) {
            throw new IllegalArgumentException("이미 삭제된 허브입니다.");
        }

        hub.softDelete("master"); // TODO: 나중에 로그인한 유저 ID로 교체
    }
}