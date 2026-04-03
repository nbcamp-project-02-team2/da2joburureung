package com.da2jobu.application.client;

import java.util.UUID;

public interface OrderClient {
    boolean hasActiveOrders(UUID companyId);
}