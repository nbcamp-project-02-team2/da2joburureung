package com.da2jobu.deliveryservice.application.delivery.service;

import com.da2jobu.deliveryservice.application.delivery.command.CreateDeliveryFromOrderCommand;

public interface CreateDeliveryFromOrderService {

    void execute(CreateDeliveryFromOrderCommand command);
}
