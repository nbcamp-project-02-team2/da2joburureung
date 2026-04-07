package com.delivery.hub.domain.repository;

import com.delivery.hub.application.dto.SearchHubCommand;
import com.delivery.hub.interfaces.dto.Respone.HubResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HubRepositoryCustom {
    Page<HubResponse> searchHubs(SearchHubCommand command, Pageable pageable);

}