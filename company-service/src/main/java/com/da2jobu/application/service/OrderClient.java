package com.da2jobu.application.service;

import java.util.UUID;

public interface OrderClient {
    boolean hasActiveOrders(UUID companyId);
}