package com.da2jobu.infrastructure.persistence;

import com.da2jobu.domain.model.entity.Company;
import com.da2jobu.domain.model.entity.QCompany;
import com.da2jobu.domain.model.vo.CompanyId;
import com.da2jobu.domain.model.vo.CompanyType;
import com.da2jobu.domain.repository.CompanyRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@RequiredArgsConstructor
public class CompanyRepositoryAdapter implements CompanyRepository {

    private final JpaCompanyRepository jpaCompanyRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    public Company save(Company company) {
        return jpaCompanyRepository.save(company);
    }

    @Override
    public Optional<Company> findByIdAndDeletedAtIsNull(UUID companyId) {
        return jpaCompanyRepository.findByCompanyIdAndDeletedAtIsNull(CompanyId.of(companyId));
    }

    @Override
    public Page<Company> search(String name, CompanyType type, UUID hubId, Pageable pageable) {
        QCompany qCompany = QCompany.company;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qCompany.deletedAt.isNull());

        if (name != null && !name.isBlank()) {
            builder.and(qCompany.name.containsIgnoreCase(name));
        }
        if (type != null) {
            builder.and(qCompany.type.eq(type));
        }
        if (hubId != null) {
            builder.and(qCompany.hubId.hubId.eq(hubId));
        }

        List<Company> content = queryFactory
                .selectFrom(qCompany)
                .where(builder)
                .orderBy(qCompany.createdAt.desc())
                .offset(pageable.getOffset())   //시작 위치
                .limit(pageable.getPageSize())  //페이지 건 수
                .fetch();

        Long total = queryFactory
                .select(qCompany.count())
                .from(qCompany)
                .where(builder)
                .fetchOne();
        if (total == null) {
            total = 0L;
        }

        return new PageImpl<>(content, pageable, total);
    }
}