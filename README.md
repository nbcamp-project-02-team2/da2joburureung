# 다2조부 (da2joburureung)

다2조부는 **허브 기반 물류·주문·배송 도메인**을 MSA로 분리해 구현한 백엔드 프로젝트입니다.
Spring Boot 기반 멀티모듈 구조 위에 Eureka, Gateway, Kafka, Redis, PostgreSQL을 연결해 서비스 간 통신과 비동기 이벤트 흐름을 구성했습니다.

---

## 목차

1. [프로젝트 개요](#1-프로젝트-개요)
2. [아키텍처](#2-아키텍처)
3. [서비스 구성](#3-서비스-구성)
4. [코드 구조](#4-코드-구조)
5. [주요 기능](#5-주요-기능)
6. [기술 스택](#6-기술-스택)
7. [실행 환경](#7-실행-환경)
8. [빠른 시작](#8-빠른-시작)
9. [API 목록](#9-api-목록)
10. [이벤트 흐름](#10-이벤트-흐름)

---

## 1. 프로젝트 개요

주문 생성 이후 상품·업체·허브·배송 서비스가 서로 연동되는 흐름을 중심으로 설계된 **물류/배송 플랫폼 백엔드**입니다.

루트에서 여러 서비스를 함께 관리하는 멀티모듈 Gradle 프로젝트로 구성되어 있으며, 다음 모듈을 포함합니다.

| 모듈 | 설명 |
|---|---|
| `common` | 공통 응답, 예외, 유틸리티 |
| `eureka-server` | 서비스 디스커버리 |
| `gateway-service` | API 진입점, JWT 검증 |
| `user-service` | 회원/인증 |
| `company-service` | 업체 관리 |
| `hub-service` | 허브 관리 |
| `hubpath-service` | 허브 간 경로 관리 |
| `product-service` | 상품 관리 |
| `order-service` | 주문 처리 |
| `delivery-service` | 배송 처리 |
| `notification-service` | 알림 |
| `ai-service` | AI 배송 이력 |

---

## 2. 아키텍처

```
클라이언트
    │
    ▼
[Gateway :8080]  ── JWT 검증 ──▶ 각 서비스로 라우팅
    │
    ├── user-service       :8082
    ├── hub-service        :8084
    ├── hubpath-service    :8086
    ├── company-service    :8088
    ├── product-service    :8090
    ├── order-service      :8092
    ├── delivery-service   :8094
    ├── notification-service :8100
    └── ai-service

[Eureka :8761]   ── 서비스 등록/디스커버리
[Kafka  :9092]   ── 비동기 이벤트 (주문 수락 → 배송 생성 → 알림)
[Redis]          ── 캐시
[PostgreSQL]     ── 각 서비스 DB (스키마 분리)
```

**서비스 간 통신 방식**

- 동기 호출: OpenFeign (`/api/internal/**` 내부 API)
- 비동기 이벤트: Kafka

---

## 3. 서비스 구성

### `common`
공통 응답 객체(`CommonResponse`), 예외, 유틸리티를 여러 서비스에서 공유하기 위한 라이브러리 모듈입니다.

### `eureka-server`
각 마이크로서비스를 등록하고 조회하는 서비스 디스커버리 서버입니다.

### `gateway-service`
외부 요청의 진입점으로, JWT 검증 후 `X-Username`, `X-User-Role` 헤더를 추가해 하위 서비스로 라우팅합니다.

### `user-service`
회원가입/로그인, 사용자 조회/승인/거절/삭제, 내부 사용자 조회 기능을 담당합니다.
`AuthController`, `UserController`, `UserInternalController`가 분리되어 있습니다.

### `company-service`
업체 도메인을 담당하며 외부용 `CompanyController`와 내부 서비스 호출용 `CompanyInternalController`가 분리되어 있습니다.
Feign, Kafka, Redis, QueryDSL, Resilience4j를 사용합니다.

### `hub-service`
허브 정보를 관리합니다. Redis 캐시와 Feign을 사용하며, `/api/internal/hubs` 경로로 타 서비스에 허브 정보를 제공합니다.

### `hubpath-service`
허브 간 최단 경로를 계산하고 관리합니다. Kakao 주소 API를 통해 거리/소요 시간을 산출하며 Redis로 결과를 캐시합니다.

### `product-service`
상품 생성/수정/삭제/조회와 가격 이력 관리를 담당합니다.
`ProductController`, `ProductInternalController`가 분리되어 있으며 QueryDSL 기반 검색을 지원합니다.

### `order-service`
주문 생성/수정/취소/삭제/조회를 담당합니다.
주문 수락 시 `OrderAcceptedEvent`를 Kafka로 발행합니다.

### `delivery-service`
Kafka에서 `OrderAcceptedEvent`를 수신해 배송을 생성합니다.
배송(`delivery`), 배송 담당자(`deliveryManager`), 배송 경로 기록(`deliveryRouteRecord`) 계층이 분리되어 있습니다.

### `notification-service`
Kafka 이벤트를 수신해 알림을 생성하고 발송합니다.

### `ai-service`
AI 기반 배송 이력 조회 기능을 제공합니다. Spring AI + OpenAI, pgvector를 사용합니다.

---

## 4. 코드 구조

전반적으로 아래 레이어 구조를 따릅니다.

```
{service}/
├── application/      # 유스케이스, 서비스 로직
├── domain/           # 엔티티, 도메인 모델, 리포지토리 인터페이스
├── infrastructure/   # JPA 구현체, Feign 클라이언트, Kafka 설정
└── interfaces/       # 컨트롤러, 요청/응답 DTO
    (또는 presentation/)
```

---

## 5. 주요 기능

### 사용자 / 인증
- 회원가입 / 로그인 (JWT 발급)
- 내 정보 조회
- 사용자 단건 조회, 목록 조회
- 사용자 승인 / 거절 / 삭제

### 상품
- 상품 생성 / 수정 / 삭제 (Soft Delete)
- 상품 단건 조회, 목록/검색 (QueryDSL)
- 상품 가격 이력 조회
- 내부 서비스용 상품 조회 API

### 주문
- 주문 생성 / 수정 / 취소 / 삭제 (Soft Delete)
- 주문 단건 조회, 목록 검색
- 업체의 진행 중 주문 수 조회 (내부 API)
- 주문 수락 시 Kafka 이벤트 발행

### 배송
- Kafka 이벤트 기반 자동 배송 생성
- 허브 경로 조회를 통한 배송 경로 기록 생성
- 배송 담당자 배정 관리
- 배송 상태 관리

### 허브 / 허브 경로 / 업체 / 알림
- 허브 관리 API (생성/수정/삭제/조회)
- 허브 간 최단 경로 계산 및 관리
- 업체 외부/내부 API
- Kafka 기반 알림 발송

---

## 6. 기술 스택

### Backend
| 항목 | 버전/내용 |
|---|---|
| Java | 17 |
| Spring Boot | 3.5.12 |
| Spring Cloud | 2025.0.1 |
| Spring Web / WebFlux | - |
| Spring Data JPA | - |
| Spring Security + JWT | - |
| OpenFeign | 서비스 간 내부 통신 |
| Eureka Client / Server | 서비스 디스커버리 |
| Spring Kafka | 비동기 메시징 |
| Redis | 캐시 |
| QueryDSL | 동적 쿼리 |
| Springdoc OpenAPI | Swagger UI |
| Spring AI + OpenAI | AI 서비스 |

### Infrastructure
| 항목 | 용도 |
|---|---|
| PostgreSQL 17 + pgvector | 서비스별 DB, 벡터 검색 |
| Redis | 캐시 |
| Kafka + Zookeeper | 비동기 이벤트 |
| Kafka UI | Kafka 모니터링 |
| Docker Compose | 전체 서비스 실행 |

---

## 7. 실행 환경

`docker-compose.yml` 기준으로 다음 컨테이너가 함께 실행됩니다.

| 컨테이너 | 기본 포트 |
|---|---|
| zookeeper | 2181 |
| postgres | 5433 (host) |
| eureka-server | 8761 |
| gateway-service | 8080 |
| user-service | 8082 |
| hub-service | 8084 |
| hubpath-service | 8086 |
| company-service | 8088 |
| product-service | 8090 |
| order-service | 8092 |
| delivery-service | 8094 |
| notification-service | 8100 |
| redis | 6379 |
| kafka | 9092 |
| kafka-ui | 8071 |

각 서비스별 환경변수(포트, DB URL, Kafka 주소, Eureka 주소, JWT secret 등)는 `.env` 파일을 통해 주입됩니다.

---

## 8. 빠른 시작

### 1) 저장소 클론

```bash
git clone https://github.com/nbcamp-project-02-team2/da2joburureung.git
cd da2joburureung
git checkout develop
```

### 2) 환경 변수 파일 준비

루트에 `.env` 파일을 생성하고 아래 항목을 채웁니다.

```env
POSTGRES_USER=da2jobu
POSTGRES_PASSWORD=your_password

USER_DB=user
NOTIFICATION_DB=notification
DELIVERY_DB=delivery
COMPANY_DB=company
HUB_DB=hub
PRODUCT_DB=product
ORDER_DB=order

EUREKA_SERVER_PORT=8761
GATEWAY_SERVER_PORT=8080
USER_SERVER_PORT=8082
HUB_SERVER_PORT=8084
HUB_PATH_SERVER_PORT=8086
COMPANY_SERVER_PORT=8088
PRODUCT_SERVER_PORT=8090
ORDER_SERVER_PORT=8092
DELIVERY_SERVER_PORT=8094
NOTIFICATION_SERVER_PORT=8100

ZOOKEEPER_CLIENT_PORT=2181
KAFKA_PORT=9092
KAFKA_EXTERNAL_PORT=29092
KAFKA_UI_PORT=8071
SPRING_DATA_REDIS_PORT=6379

JWT_SECRET=your_jwt_secret
KAKAO_API_KEY=your_kakao_api_key
INTERNAL_TOKEN=your_internal_token
INTERNAL_API_SECRET=your_internal_api_secret
```

### 3) 전체 서비스 실행

```bash
docker compose up --build
```

### 4) 주요 접속 주소

| 서비스 | 주소 |
|---|---|
| Eureka Dashboard | http://localhost:8761 |
| API Gateway | http://localhost:8080 |
| Kafka UI | http://localhost:8071 |
| Swagger (예: order) | http://localhost:8092/swagger-ui/index.html |

> 각 서비스의 Swagger UI는 `http://localhost:{서비스포트}/swagger-ui/index.html`에서 접근할 수 있습니다.

### 5) 초기 데이터 설정

허브 경로 데이터는 자동으로 생성되지 않습니다. 첫 실행 후 hubpath-service Swagger에서 `POST /api/internal/hub-paths`를 호출해 허브 간 경로를 생성해야 배송 흐름이 정상 동작합니다.

---

## 9. API 목록

> 모든 API는 Gateway(`http://localhost:8080`)를 통해 호출합니다.
> 권한 검사는 Gateway가 주입하는 `X-User-Role` 헤더 기준입니다.

### 인증 (user-service :8082)

| 메서드 | 경로 | 설명 | 권한 |
|---|---|---|---|
| POST | `/api/auth/signup` | 회원가입 | 없음 |
| POST | `/api/auth/login` | 로그인 (JWT 발급) | 없음 |

### 사용자 (user-service :8082)

| 메서드 | 경로 | 설명 | 권한 |
|---|---|---|---|
| GET | `/api/users/me` | 내 정보 조회 | 인증된 사용자 |
| PATCH | `/api/users/me` | 내 정보 수정 | 인증된 사용자 |
| PATCH | `/api/users/me/password` | 내 비밀번호 변경 | 인증된 사용자 |
| GET | `/api/users/{userId}` | 사용자 단건 조회 | MASTER |
| GET | `/api/users` | 사용자 목록 조회 | MASTER |
| PATCH | `/api/users/{userId}/approvals` | 사용자 승인 | MASTER, HUB_MANAGER |
| PATCH | `/api/users/{userId}/rejection` | 사용자 거절 | MASTER, HUB_MANAGER |
| DELETE | `/api/users/{userId}` | 사용자 삭제 | MASTER |

### 업체 (company-service :8088)

| 메서드 | 경로 | 설명 | 권한 |
|---|---|---|---|
| POST | `/api/companies` | 업체 생성 | MASTER, HUB_MANAGER |
| PUT | `/api/companies/{companyId}` | 업체 수정 | MASTER, HUB_MANAGER, COMPANY_MANAGER |
| GET | `/api/companies` | 업체 목록 조회 | 인증된 사용자 |
| GET | `/api/companies/{companyId}` | 업체 단건 조회 | 인증된 사용자 |
| DELETE | `/api/companies/{companyId}` | 업체 삭제 | MASTER, HUB_MANAGER |

### 허브 (hub-service :8084)

| 메서드 | 경로 | 설명 | 권한 |
|---|---|---|---|
| POST | `/api/internal/hubs` | 허브 생성 | MASTER |
| GET | `/api/internal/hubs` | 허브 목록/검색 | 인증된 사용자 |
| GET | `/api/internal/hubs/{hub_id}` | 허브 단건 조회 | 인증된 사용자 |
| GET | `/api/internal/hubs/all` | 전체 허브 목록 | 인증된 사용자 |
| PATCH | `/api/internal/hubs/{hub_id}` | 허브 수정 | MASTER |
| DELETE | `/api/internal/hubs/{hub_id}` | 허브 삭제 | MASTER |

### 허브 경로 (hubpath-service :8086)

| 메서드 | 경로 | 설명 | 권한 |
|---|---|---|---|
| POST | `/api/internal/hub-paths` | 허브 경로 생성 | MASTER |
| GET | `/api/internal/hub-paths` | 허브 경로 목록/검색 | 인증된 사용자 |
| GET | `/api/internal/hub-paths/{hubPathId}` | 허브 경로 단건 조회 | 인증된 사용자 |
| GET | `/api/internal/hub-paths/search?departHubName=&arriveHubName=` | 허브 이름으로 경로 검색 | 인증된 사용자 |
| PATCH | `/api/internal/hub-paths/{hubPathId}` | 허브 경로 수정 | MASTER |
| DELETE | `/api/internal/hub-paths/{hubPathId}` | 허브 경로 삭제 | MASTER |

### 상품 (product-service :8090)

| 메서드 | 경로 | 설명 | 권한 |
|---|---|---|---|
| POST | `/api/products` | 상품 생성 | MASTER, HUB_MANAGER, COMPANY_MANAGER |
| PATCH | `/api/products/{productId}` | 상품 수정 | MASTER, HUB_MANAGER, COMPANY_MANAGER |
| DELETE | `/api/products/{productId}` | 상품 삭제 | MASTER, HUB_MANAGER |
| GET | `/api/products/{productId}` | 상품 단건 조회 | 인증된 사용자 |
| GET | `/api/products` | 상품 목록/검색 | 인증된 사용자 |
| GET | `/api/products/{productId}/price-histories` | 상품 가격 이력 조회 | 인증된 사용자 |

### 주문 (order-service :8092)

| 메서드 | 경로 | 설명 | 권한 |
|---|---|---|---|
| POST | `/api/orders` | 주문 생성 | 인증된 사용자 |
| PATCH | `/api/orders/{orderId}` | 주문 수정 | 인증된 사용자 |
| PATCH | `/api/orders/{orderId}/cancel` | 주문 취소 | 인증된 사용자 |
| DELETE | `/api/orders/{orderId}` | 주문 삭제 (Soft Delete) | MASTER |
| GET | `/api/orders/{orderId}` | 주문 단건 조회 | 인증된 사용자 |
| GET | `/api/orders` | 주문 목록 검색 | 인증된 사용자 |

### 배송 (delivery-service :8094)

| 메서드 | 경로 | 설명 | 권한 |
|---|---|---|---|
| GET | `/api/deliveries/{deliveryId}` | 배송 단건 조회 | MASTER, HUB_MANAGER, DELIVERY_MANAGER, COMPANY_MANAGER |
| GET | `/api/deliveries` | 배송 목록 조회 | MASTER, HUB_MANAGER, DELIVERY_MANAGER, COMPANY_MANAGER |
| PUT | `/api/deliveries/{deliveryId}/status` | 배송 상태 변경 | MASTER, HUB_MANAGER, DELIVERY_MANAGER |
| DELETE | `/api/deliveries/{deliveryId}` | 배송 삭제 | MASTER, HUB_MANAGER |

### 배송 담당자 (delivery-service :8094)

| 메서드 | 경로 | 설명 | 권한 |
|---|---|---|---|
| POST | `/api/delivery-managers` | 배송 담당자 생성 | MASTER, HUB_MANAGER |
| GET | `/api/delivery-managers/{deliveryManagerId}` | 배송 담당자 단건 조회 | MASTER, HUB_MANAGER, DELIVERY_MANAGER |
| PATCH | `/api/delivery-managers/{deliveryManagerId}` | 배송 담당자 수정 | MASTER, HUB_MANAGER |
| DELETE | `/api/delivery-managers/{deliveryManagerId}` | 배송 담당자 삭제 | MASTER, HUB_MANAGER |
| GET | `/api/delivery-managers` | 배송 담당자 목록 조회 | MASTER, HUB_MANAGER, DELIVERY_MANAGER |
| GET | `/api/delivery-managers/{deliveryManagerId}/assignments` | 배정 이력 조회 | MASTER, HUB_MANAGER, DELIVERY_MANAGER |

### 배송 경로 기록 (delivery-service :8094)

| 메서드 | 경로 | 설명 | 권한 |
|---|---|---|---|
| GET | `/api/delivery-routes/{routeRecordId}` | 경로 기록 단건 조회 | MASTER, HUB_MANAGER, DELIVERY_MANAGER, COMPANY_MANAGER |
| GET | `/api/deliveries/{deliveryId}/routes` | 배송별 경로 목록 조회 | MASTER, HUB_MANAGER, DELIVERY_MANAGER, COMPANY_MANAGER |
| PUT | `/api/delivery-routes/{routeRecordId}/status` | 경로 상태 변경 | MASTER, HUB_MANAGER, DELIVERY_MANAGER |
| PUT | `/api/delivery-routes/{routeRecordId}/metrics` | 경로 실적 갱신 | MASTER, HUB_MANAGER, DELIVERY_MANAGER |
| DELETE | `/api/delivery-routes/{routeRecordId}` | 경로 기록 삭제 | MASTER, HUB_MANAGER |

### 알림 (notification-service :8100)

| 메서드 | 경로 | 설명 | 권한 |
|---|---|---|---|
| POST | `/api/notifications/users` | 슬랙 메시지 발송 | 인증된 사용자 |
| GET | `/api/notifications` | 슬랙 메시지 목록 조회 (커서 기반) | 인증된 사용자 |
| DELETE | `/api/notifications/{messageId}` | 슬랙 메시지 삭제 | 인증된 사용자 |

### AI (ai-service)

| 메서드 | 경로 | 설명 | 권한 |
|---|---|---|---|
| GET | `/api/ai` | AI 배송 이력 조회 | 인증된 사용자 |

---

## 10. 이벤트 흐름

### 주문 수락 → 배송 생성

```
[order-service]
  주문 수락
    │
    │ Kafka: order.accepted.v1
    ▼
[delivery-service]
  OrderAcceptedEventListener
    │
    ├── company-service (Feign) → 공급업체/수령업체 허브 ID 조회
    ├── hub-service (Feign)     → 허브 정보 조회
    ├── hubpath-service (Feign) → 허브 간 경로 조회
    └── 배송 + 배송 경로 기록 생성
          │
          │ Kafka: DeliveryCreatedEvent
          ▼
      [order-service] 주문 상태 업데이트
```