package com.delivery.hubpath.application.service;

import com.delivery.hubpath.application.dto.CreateHubPathCommand;
import com.delivery.hubpath.domain.model.HubPath;
import com.delivery.hubpath.domain.repository.HubPathRepository;
import com.delivery.hubpath.infrastructure.client.HubClient;
import com.delivery.hubpath.infrastructure.client.HubResponse;
import com.delivery.hubpath.infrastructure.client.PageResponse;
import com.delivery.hubpath.interfaces.dto.request.SearchHubPathRequest;
import com.delivery.hubpath.interfaces.dto.response.HubPathResponse;
import common.client.KakaoAddressService;
import common.dto.CommonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

        HubResponse departHub = fetchHubByName(command.departHubName());
        HubResponse arriveHub = fetchHubByName(command.arriveHubName());

        CommonResponse<List<HubResponse>> response = hubClient.getAllHubs();
        List<HubResponse> allHubs = response.getData();
        log.info("알고리즘에 사용될 활성 허브 개수: {}", allHubs.size()); // 여기서 10개가 찍힌다면 페이징 문제!

        HubPath hubPath = HubPath.createPath(departHub, arriveHub, allHubs, kakaoAddressService);

        HubPath savedPath = hubPathRepository.save(hubPath);

        return HubPathResponse.from(savedPath);
    }

    private HubResponse fetchHubByName(String hubName) {
        CommonResponse<PageResponse<HubResponse>> response = hubClient.getHubs(hubName, null, 10, 0);

        List<HubResponse> content = response.getData().getContent();

        if (content == null || content.isEmpty()) {
            throw new IllegalArgumentException("해당 이름의 허브를 찾을 수 없습니다: " + hubName);
        }

        return content.get(0);
    }

    @Transactional(readOnly = true)
    public Page<HubPathResponse> searchHubPaths(SearchHubPathRequest request, Pageable pageable) {

        String departName = request.depart_hub_name() != null ? request.depart_hub_name() : "";
        String arriveName = request.arrive_hub_name() != null ? request.arrive_hub_name() : "";

        Page<HubPath> pageResult = hubPathRepository.findByDepartHubNameContainingAndArriveHubNameContaining(
                departName, arriveName, pageable
        );

        return pageResult.map(HubPathResponse::from);
    }
}
