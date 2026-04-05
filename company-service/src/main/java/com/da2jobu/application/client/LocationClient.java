package com.da2jobu.application.client;

import com.da2jobu.domain.model.vo.Location;

public interface LocationClient {
    Location resolveLocation(String address);

}
