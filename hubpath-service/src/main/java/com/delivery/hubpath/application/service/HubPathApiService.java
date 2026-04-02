package com.delivery.hubpath.application.service;

import com.delivery.hubpath.application.dto.CreateHubPathCommand;
import com.delivery.hubpath.application.dto.UpdateHubPathCommand;
import com.delivery.hubpath.domain.model.HubPath;
import com.delivery.hubpath.domain.repository.HubPathRepository;
import com.delivery.hubpath.infrastructure.client.HubClient;
import com.delivery.hubpath.infrastructure.client.HubResponse;
import com.delivery.hubpath.infrastructure.client.PageResponse;
import com.delivery.hubpath.infrastructure.config.RestPage;
import com.delivery.hubpath.interfaces.dto.request.SearchHubPathRequest;
import com.delivery.hubpath.interfaces.dto.response.HubPathResponse;
import common.client.KakaoAddressService;
import common.dto.CommonResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class HubPathApiService {

    private final HubClient hubClient;
    private final HubPathRepository hubPathRepository;
    private final KakaoAddressService kakaoAddressService;

    // 허브 간의 경로 저장
    @CacheEvict(cacheNames = "hubPages", allEntries = true)
    @Transactional
    public HubPathResponse createHubPath(CreateHubPathCommand command) {

        HubResponse departHub = fetchHubByName(command.departHubName());
        HubResponse arriveHub = fetchHubByName(command.arriveHubName());

        List<HubResponse> allHubs = fetchAllHubs();

        HubPath hubPath = HubPath.createPath(departHub, arriveHub, allHubs, kakaoAddressService);
        HubPath savedPath = hubPathRepository.save(hubPath);

        return HubPathResponse.from(savedPath);
    }

    // 전체 허브 이동 조회 혹은 검색으로 허브 검색
    @Cacheable(cacheNames = "hubPathPages", key = "#request.toString() + #pageable.pageNumber")
    @Transactional(readOnly = true)
    public Page<HubPathResponse> searchHubPaths(SearchHubPathRequest request, Pageable pageable) {

        String departName = request.depart_hub_name() != null ? request.depart_hub_name() : "";
        String arriveName = request.arrive_hub_name() != null ? request.arrive_hub_name() : "";

        Page<HubPath> pageResult = hubPathRepository.findByDepartHubNameContainingAndArriveHubNameContaining(
                departName, arriveName, pageable
        );
        List<HubPathResponse> dtoList = pageResult.map(HubPathResponse::from).getContent();

        return new RestPage<>(dtoList, pageable, pageResult.getTotalElements());
    }

    // 특정 허브 이동간 디테일 정보 검색
    @Transactional(readOnly = true)
    public HubPathResponse getHubPathDetail(UUID hubPathId) {
        HubPath hubPath = hubPathRepository.findById(hubPathId)
                .orElseThrow(() -> new EntityNotFoundException("경로 정보를 찾을 수 없습니다. ID: " + hubPathId));

        return HubPathResponse.detailFrom(hubPath);
    }

    // 허브 간의 이동 경로 수정
    @Caching(evict = {
            @CacheEvict(cacheNames = "hubPathDetail", key = "#command.hub_path_id()"),
            @CacheEvict(cacheNames = "hubPathPages", allEntries = true)
    })
    @Transactional
    public HubPathResponse updateHubPath(UpdateHubPathCommand command) {

        HubPath hubPath = hubPathRepository.findById(command.hub_path_id())
                .orElseThrow(() -> new EntityNotFoundException("해당 경로를 찾을 수 없습니다. ID: " + command.hub_path_id()));

        String finalDepartName = (command.departHubName() != null) ? command.departHubName() : hubPath.getDepartHubName();
        String finalArriveName = (command.arriveHubName() != null) ? command.arriveHubName() : hubPath.getArriveHubName();

        if (finalDepartName.equals(hubPath.getDepartHubName()) && finalArriveName.equals(hubPath.getArriveHubName())) {
            return HubPathResponse.detailFrom(hubPath);
        }

        HubResponse departHub = fetchHubByName(finalDepartName);
        HubResponse arriveHub = fetchHubByName(finalArriveName);

        List<HubResponse> allHubs = fetchAllHubs();

        hubPath.updatePath(departHub, arriveHub, allHubs, kakaoAddressService);

        return HubPathResponse.detailFrom(hubPath);
    }

    // 허브 간 경로 삭제
    @Caching(evict = {
            @CacheEvict(cacheNames = "hubPathDetail", key = "#hubPathId"),
            @CacheEvict(cacheNames = "hubPathPages", allEntries = true)
    })
    @Transactional
    public void deleteHubPath(UUID hubPathId) {
        HubPath hubPath = hubPathRepository.findById(hubPathId)
                .orElseThrow(() -> new EntityNotFoundException("삭제할 경로 정보를 찾을 수 없습니다. ID: " + hubPathId));
        if (hubPath.isDeleted()) {
            throw new IllegalStateException("이미 삭제된 경로입니다.");
        }

        hubPath.softDelete("master"); // TODO: 나중에 로그인한 유저 ID로 교체
    }

    private HubResponse fetchHubByName(String hubName) {
        CommonResponse<PageResponse<HubResponse>> response = hubClient.getHubs(hubName, null, 10, 0);

        if (response == null || response.getData() == null || response.getData().getContent().isEmpty()) {
            throw new IllegalArgumentException("해당 이름의 허브를 찾을 수 없습니다: " + hubName);
        }

        List<HubResponse> content = response.getData().getContent();

        return content.stream()
                .filter(hub -> hub.getHub_name().equals(hubName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "유사한 이름의 허브는 검색되었으나, 정확히 일치하는 '" + hubName + "' 허브가 존재하지 않습니다."
                ));
    }

    private List<HubResponse> fetchAllHubs() {
        CommonResponse<List<HubResponse>> response = hubClient.getAllHubs();
        if (response == null || response.getData() == null) {
            throw new IllegalStateException("전체 허브 목록을 가져오는 데 실패했습니다.");
        }
        return response.getData();
    }

}
