package com.da2jobu.deliveryservice.application.deliveryManager.service;

import com.da2jobu.deliveryservice.application.deliveryManager.dto.VrptwInput;
import com.da2jobu.deliveryservice.application.deliveryManager.dto.VrptwResult;


public interface RouteOptimizationService {

    VrptwResult solve(VrptwInput input);
}
