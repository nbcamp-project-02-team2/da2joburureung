package com.da2jobu.deliveryservice.domain.vo;

public enum DeliveryStatus {
    HUB_WAITING,                 // 허브 대기중
    HUB_MOVING,                  // 허브 이동중
    ARRIVED_AT_DESTINATION_HUB,  // 목적지 허브 도착
    OUT_FOR_DELIVERY,            // 업체 이동중 / 최종 배송중
    DELIVERED                    // 배송 완료
}
