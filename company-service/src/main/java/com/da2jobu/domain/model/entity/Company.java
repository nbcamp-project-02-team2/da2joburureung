package com.da2jobu.domain.model.entity;

import com.da2jobu.domain.model.vo.CompanyType;
import com.da2jobu.domain.model.vo.Location;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "p_company")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "company_id")
    private UUID companyId;

    @Column(name = "manager_id", nullable = false)
    private UUID managerId;

    @Column(name = "hub_id", nullable = false)
    private UUID hubId;

    @Column(nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CompanyType type;

    @Embedded
    private Location location;

    // ── Audit Fields ──────────────────────────────────────────────────────────
    // TODO: 공통모듈 BaseEntity 완성 시 해당 필드들을 BaseEntity 상속으로 교체
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false, length = 100)
    private String createdBy;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by", length = 100)
    private String deletedBy;

    // ── Factory Method ────────────────────────────────────────────────────────
    public static Company create(
            UUID managerId,
            UUID hubId,
            String name,
            CompanyType type,
            Location location
    ) {
        Company company = new Company();
        company.managerId = managerId;
        company.hubId = hubId;
        company.name = name;
        company.type = type;
        company.location = location;
        return company;
    }

    // ── Business Methods ──────────────────────────────────────────────────────
    public void delete(String deletedBy) {
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}