package com.delivery.hubpath.application.service;

import com.delivery.hubpath.application.dto.CreateHubPathCommand;
import com.delivery.hubpath.application.dto.UpdateHubPathCommand;
import com.delivery.hubpath.domain.model.HubPath;
import com.delivery.hubpath.domain.repository.HubPathRepository;
import com.delivery.hubpath.domain.service.HubPathService;
import com.delivery.hubpath.infrastructure.client.HubClient;
import com.delivery.hubpath.infrastructure.client.HubResponse;
import com.delivery.hubpath.infrastructure.client.PageResponse;
import com.delivery.hubpath.infrastructure.config.RestPage;
import com.delivery.hubpath.interfaces.dto.request.SearchHubPathRequest;
import com.delivery.hubpath.interfaces.dto.response.HubPathResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class HubPathApiService {

    private final HubClient hubClient;
    private final HubPathRepository hubPathRepository;
    private final KakaoAddressService kakaoAddressService;
    private final HubPathService hubPathService;

    // 허브 간의 경로 저장
    @CacheEvict(cacheNames = "hubPages", allEntries = true)
    @Transactional
    public HubPathResponse createHubPath(CreateHubPathCommand command, String userRole, String username) {
        validateMasterRole(userRole);

        HubResponse departHub = fetchHubById(command.departHubId());
        HubResponse arriveHub = fetchHubById(command.arriveHubId());
        List<HubResponse> allHubs = fetchAllHubs();

        HubPath savedPath = hubPathService.createAndSavePath(departHub, arriveHub, allHubs);

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
    public HubPathResponse getHubPathDetail(UUID hubPathId, String departHubName, String arriveHubName) {
        if (hubPathId != null) {
            return hubPathRepository.findById(hubPathId)
                    .map(HubPathResponse::detailFrom)
                    .orElseThrow(() -> new EntityNotFoundException("경로를 찾을 수 없습니다."));
        }

        if (departHubName == null || arriveHubName == null) {
            throw new IllegalArgumentException("hubPathId가 없는 경우 departHubName과 arriveHubName은 필수입니다.");
        }

        return hubPathRepository.findTop1ByDepartHubNameContainingAndArriveHubNameContainingOrderByCreatedAtDesc(departHubName, arriveHubName)
                .map(HubPathResponse::detailFrom)
                .orElseThrow(() -> new EntityNotFoundException("해당 조건의 최신 경로가 없습니다."));
    }

    // 허브 간의 이동 경로 수정
    @Caching(evict = {
            @CacheEvict(cacheNames = "hubPathDetail", key = "#command.hub_path_id()"),
            @CacheEvict(cacheNames = "hubPathPages", allEntries = true)
    })
    @Transactional
    public HubPathResponse updateHubPath(UpdateHubPathCommand command, String userRole, String username) {
        validateMasterRole(userRole);

        HubPath hubPath = hubPathRepository.findById(command.hub_path_id())
                .orElseThrow(() -> new EntityNotFoundException("해당 경로를 찾을 수 없습니다. ID: " + command.hub_path_id()));

        UUID finalDepartId = (command.departHubId() != null) ? command.departHubId() : hubPath.getDepartHubId();
        UUID finalArriveId = (command.arriveHubId() != null) ? command.arriveHubId() : hubPath.getArriveHubId();

        HubResponse departHub = fetchHubById(finalDepartId);
        HubResponse arriveHub = fetchHubById(finalArriveId);
        List<HubResponse> allHubs = fetchAllHubs();

        HubPath newCalculatedPath = hubPathService.createAndSavePath(departHub, arriveHub, allHubs);

        hubPath.updateRouteInfo(
                newCalculatedPath.getDepartHubId(),
                newCalculatedPath.getDepartHubName(),
                newCalculatedPath.getArriveHubId(),
                newCalculatedPath.getArriveHubName()
        );

        hubPath.updateTotalInfo(newCalculatedPath.getTotalDistance(), newCalculatedPath.getTotalDuration());

        hubPath.getPathSteps().clear();
        newCalculatedPath.getPathSteps().forEach(hubPath::addStep);

        hubPathRepository.delete(newCalculatedPath);

        hubPath.setUpdatedBy(username);

        return HubPathResponse.detailFrom(hubPath);
    }

    // 허브 간 경로 삭제
    @Caching(evict = {
            @CacheEvict(cacheNames = "hubPathDetail", key = "#hubPathId"),
            @CacheEvict(cacheNames = "hubPathPages", allEntries = true)
    })
    @Transactional
    public void deleteHubPath(UUID hubPathId, String userRole, String username) {
        validateMasterRole(userRole);

        HubPath hubPath = hubPathRepository.findById(hubPathId)
                .orElseThrow(() -> new EntityNotFoundException("삭제할 경로 정보를 찾을 수 없습니다. ID: " + hubPathId));
        if (hubPath.isDeleted()) {
            throw new IllegalStateException("이미 삭제된 경로입니다.");
        }

        hubPath.softDelete(username);
    }

    private void validateMasterRole(String userRole) {
        if (!"MASTER".equals(userRole)) throw new RuntimeException("MASTER 권한만 접근 가능합니다.");
    }

    private HubResponse fetchHubById(UUID hubId) {
        CommonResponse<PageResponse<HubResponse>> response = hubClient.getHubs(hubId, 1, 0);
        if (response == null || response.getData() == null) {
            throw new EntityNotFoundException("허브 정보를 불러올 수 없습니다. ID: " + hubId);
        }

        PageResponse<HubResponse> pageData;
        if (response.getData() instanceof java.util.Map) {
            pageData = new ObjectMapper()
                    .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
                    .convertValue(response.getData(), new com.fasterxml.jackson.core.type.TypeReference<>() {});
        } else {
            pageData = (PageResponse<HubResponse>) response.getData();
        }

        if (pageData == null || pageData.getContent() == null || pageData.getContent().isEmpty()) {
            throw new EntityNotFoundException("해당 ID의 허브가 존재하지 않습니다: " + hubId);
        }

        return pageData.getContent().get(0);
    }

    private List<HubResponse> fetchAllHubs() {
        CommonResponse<List<HubResponse>> response = hubClient.getAllHubs();
        if (response == null || response.getData() == null) throw new IllegalStateException("허브 목록 조회 실패");
        return response.getData();
    }
}