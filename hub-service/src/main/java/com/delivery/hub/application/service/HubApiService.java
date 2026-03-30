package com.delivery.hub.application.service;

import com.delivery.hub.application.dto.CreateHubCommand;
import com.delivery.hub.domain.model.Hub;
import com.delivery.hub.domain.repository.HubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HubApiService {

    private final HubRepository hubrepository;

    @Transactional
    public Hub createHub(CreateHubCommand command) {
        Hub hub = command.toEntity();
        return hubrepository.save(hub);
    }
}
