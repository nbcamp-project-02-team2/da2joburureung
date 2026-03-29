package com.da2jobu.domain.model.entity;

import com.da2jobu.domain.model.vo.*;
import common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

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
    @AttributeOverride(name = "hubId", column = @Column(name = "hub_id"))
    private HubId hubId;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CompanyType type;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "address", column = @Column(name = "address")),
            @AttributeOverride(name = "latitude", column = @Column(name = "latitude")),
            @AttributeOverride(name = "longitude", column = @Column(name = "longitude"))
    })
    private Location location;


    // ── Factory Method ────────────────────────────────────────────────────────
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
    // ── Factory Method ────────────────────────────────────────────────────────
}