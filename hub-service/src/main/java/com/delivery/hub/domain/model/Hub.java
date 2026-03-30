package com.delivery.hub.domain.model;

import common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "p_hub")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
@Schema(description = "허브 정보 엔티티")
public class Hub extends BaseEntity{

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "hub_id", updatable = false, nullable = false)
    @Schema(description = "허브 식별자 (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID hubId;

    @Column(name = "hub_name", nullable = false)
    @Schema(description = "허브 이름", example = "경기 남부 허브")
    private String hub_name;

    @Column(name = "address", nullable = false)
    @Schema(description = "허브 주소", example = "경기도 이천시 ...")
    private String address;

    @Column(name = "latitude", nullable = false, precision = 10, scale = 5)
    @Schema(description = "위도 (예: 37.1234)")
    private BigDecimal latitude;

    @Column(name = "longitude", nullable = false, precision = 10, scale = 5)
    @Schema(description = "경도 (예: 127.1234)")
    private BigDecimal longitude;

    public static Hub createHub(String hub_name, String address, BigDecimal latitude, BigDecimal longitude) {
        return Hub.builder()
                .hub_name(hub_name)
                .address(address)
                .latitude(latitude)
                .longitude(longitude)
                .createdBy("master")
                .createdAt(LocalDateTime.now())
                .build();
    }

    public void updateHub(String hub_name, String address, BigDecimal latitude, BigDecimal longitude) {
        this.hub_name = hub_name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
