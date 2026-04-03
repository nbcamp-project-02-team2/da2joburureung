package com.da2jobu.deliveryservice.application.service;

import com.da2jobu.deliveryservice.application.command.CreateDeliveryFromOrderCommand;

public interface CreateDeliveryFromOrderService {

    void execute(CreateDeliveryFromOrderCommand command);
}
