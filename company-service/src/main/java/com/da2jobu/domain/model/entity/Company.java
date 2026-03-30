package com.da2jobu.domain.model.entity;

import com.da2jobu.domain.model.vo.*;
import common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;

@Entity
@Table(name = "p_company")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Company extends BaseEntity {

    @EmbeddedId
    @AttributeOverride(name = "companyId", column = @Column(name = "company_id"))
    private CompanyId companyId;

    @Embedded
    @AttributeOverride(name = "managerId", column = @Column(name = "manager_id"))
    private ManagerId managerId;

    @Embedded
    @AttributeOverride(name = "hubId", column = @Column(name = "hub_id", nullable = false))
    private HubId hubId;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CompanyType type;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "address", column = @Column(name = "address", nullable = false)),
            @AttributeOverride(name = "latitude", column = @Column(name = "latitude", nullable = false, precision = 10, scale = 7)),
            @AttributeOverride(name = "longitude", column = @Column(name = "longitude", nullable = false, precision = 10, scale = 7))
    })
    private Location location;


    // ========== 생성 메서드 ==========
    public static Company create(
            CompanyId companyId,
            HubId hubId,
            String name,
            CompanyType type,
            Location location
    ) {
        Company company = new Company();
        company.companyId = companyId;
        company.hubId = hubId;
        company.name = name;
        company.type = type;
        company.location = location;
        return company;
    }


    // ========== 비즈니스 로직 ==========

    /**
     * 업체 담당자 배정
     * Kafka 이벤트(user.role.assigned)를 통해 유저 서비스에서 COMPANY_MANAGER 롤 부여 시 호출
     */
    public void updateManagerId(UUID managerId) {
        this.managerId = ManagerId.of(managerId);
    }

    /**
     * 업체 정보 수정
     * 업체명 변경 시 불변식 재검증
     */
    public void update(String name, CompanyType type, HubId hubId, Location location) {
        this.name = name;
        this.type = type;
        this.hubId = hubId;
        this.location = location;
    }


    // ========== 조회 메서드 ==========

    /**
     * 담당자 배정 여부 확인
     */
    public boolean hasManager() {
        return this.managerId != null;
    }

    /**
     * 특정 허브 소속 업체 여부 확인
     */
    public boolean belongsToHub(UUID hubId) {
        return this.hubId.getHubId().equals(hubId);
    }
}