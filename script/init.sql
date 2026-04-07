-- Role이 없을 때만 생성
DO
$$
BEGIN
   IF NOT EXISTS (
      SELECT FROM pg_catalog.pg_roles
      WHERE rolname = 'da2jobu'
   ) THEN
      CREATE ROLE da2jobu LOGIN PASSWORD 'p@ssw0rd';
   END IF;
END
$$;

ALTER USER da2jobu CREATEDB;

-- MSA 서비스용 데이터베이스 생성 (소유자: da2jobu)
CREATE DATABASE "user" OWNER da2jobu;
CREATE DATABASE hub OWNER da2jobu;
CREATE DATABASE company OWNER da2jobu;
CREATE DATABASE product OWNER da2jobu;
CREATE DATABASE "order" OWNER da2jobu;
CREATE DATABASE delivery OWNER da2jobu;
CREATE DATABASE "delivery-route" OWNER da2jobu;
CREATE DATABASE notification OWNER da2jobu;
CREATE DATABASE "ai-db" OWNER da2jobu;

-- -- 이후 알림 서비스 데이터를 처리하기 위해 notification DB에 연결 (psql 명령)
-- \c notification da2jobu
--
-- CREATE SCHEMA IF NOT EXISTS da2jobu AUTHORIZATION da2jobu;
-- GRANT ALL ON SCHEMA da2jobu TO da2jobu;
-- GRANT ALL ON ALL TABLES IN SCHEMA da2jobu TO da2jobu;
-- GRANT ALL ON ALL SEQUENCES IN SCHEMA da2jobu TO da2jobu;
--
-- SET search_path TO notification;

-- pgvector extension 추가 (public 스키마에 명시적으로 추가)
CREATE EXTENSION IF NOT EXISTS vector SCHEMA public;

-- uuid_generate_v4() 사용을 위한 extension 추가 (public 스키마에 명시적으로 추가)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp" SCHEMA public;

--
-- PostgreSQL database dump
--

\restrict 6D8KnLjnaPopOmaUATkQdb6N7rjahzKKwhh4rH2el97xucJxgar3XXWGnTbKKbf

-- Dumped from database version 17.9 (Debian 17.9-1.pgdg12+1)
-- Dumped by pg_dump version 17.9 (Debian 17.9-1.pgdg12+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--

SET default_tablespace = '';

SET default_table_access_method = heap;

-- =============================================
-- p_slack_message 테이블 생성
-- =============================================
CREATE TABLE IF NOT EXISTS notification.p_slack_message (
    slack_message_id    UUID            PRIMARY KEY,
    recipient_id        UUID            NOT NULL,
    message             TEXT            NOT NULL,
    message_type        VARCHAR(50)     NOT NULL,
    message_status      VARCHAR(50)     NOT NULL,
    retry_count         INTEGER         NOT NULL DEFAULT 0,
    succeeded_at        TIMESTAMP,
    created_at          TIMESTAMP       NOT NULL,
    created_by          VARCHAR(255)    NOT NULL,
    updated_at          TIMESTAMP,
    updated_by          VARCHAR(255),
    deleted_at          TIMESTAMP,
    deleted_by          VARCHAR(255)
);

-- =============================================
-- Mock 데이터 30개
-- =============================================
INSERT INTO notification.p_slack_message
    (slack_message_id, recipient_id, message, message_type, message_status, retry_count, succeeded_at, created_at, created_by, updated_at, updated_by, deleted_at, deleted_by)
VALUES
    (public.uuid_generate_v4(), '11111111-0000-0000-0000-000000000001', '주문이 접수되었습니다.', 'NOTIFICATION', 'SUCCESS', 0, '2025-03-01 09:05:00', '2025-03-01 09:00:00', 'system', '2025-03-01 09:05:00', 'system', NULL, NULL),
    (public.uuid_generate_v4(), '11111111-0000-0000-0000-000000000002', '배송이 시작되었습니다.', 'NOTIFICATION', 'SUCCESS', 0, '2025-03-02 10:10:00', '2025-03-02 10:05:00', 'system', '2025-03-02 10:10:00', 'system', NULL, NULL),
    (public.uuid_generate_v4(), '11111111-0000-0000-0000-000000000003', '안녕하세요, 공지사항입니다.', 'MESSAGE', 'SUCCESS', 0, '2025-03-03 11:01:00', '2025-03-03 11:00:00', 'admin', '2025-03-03 11:01:00', 'admin', NULL, NULL),
    (public.uuid_generate_v4(), '11111111-0000-0000-0000-000000000004', '결제가 완료되었습니다.', 'NOTIFICATION', 'SUCCESS', 0, '2025-03-04 14:20:00', '2025-03-04 14:15:00', 'system', '2025-03-04 14:20:00', 'system', NULL, NULL),
    (public.uuid_generate_v4(), '11111111-0000-0000-0000-000000000005', '슬랙 연동 테스트 메시지입니다.', 'MESSAGE', 'PENDING', 0, NULL, '2025-03-05 08:00:00', 'system', '2025-03-05 08:00:00', 'system', NULL, NULL),
    (public.uuid_generate_v4(), '11111111-0000-0000-0000-000000000006', '서비스 점검 안내드립니다.', 'NOTIFICATION', 'SUCCESS', 0, '2025-03-06 02:01:00', '2025-03-06 02:00:00', 'admin', '2025-03-06 02:01:00', 'admin', NULL, NULL),
    (public.uuid_generate_v4(), '11111111-0000-0000-0000-000000000007', '비밀번호가 변경되었습니다.', 'NOTIFICATION', 'SUCCESS', 0, '2025-03-07 13:31:00', '2025-03-07 13:30:00', 'system', '2025-03-07 13:31:00', 'system', NULL, NULL),
    (public.uuid_generate_v4(), '11111111-0000-0000-0000-000000000008', '새로운 댓글이 달렸습니다.', 'NOTIFICATION', 'FAIL', 3, NULL, '2025-03-08 15:00:00', 'system', '2025-03-08 15:05:00', 'system', NULL, NULL),
    (public.uuid_generate_v4(), '11111111-0000-0000-0000-000000000009', '팀 공지: 오늘 회의는 오후 3시입니다.', 'MESSAGE', 'SUCCESS', 0, '2025-03-09 09:01:00', '2025-03-09 09:00:00', 'admin', '2025-03-09 09:01:00', 'admin', NULL, NULL),
    (public.uuid_generate_v4(), '11111111-0000-0000-0000-000000000010', '계정 정지 안내드립니다.', 'NOTIFICATION', 'SUCCESS', 0, '2025-03-10 11:21:00', '2025-03-10 11:20:00', 'system', '2025-03-10 11:21:00', 'system', NULL, NULL),
    (public.uuid_generate_v4(), '11111111-0000-0000-0000-000000000011', '환불이 처리되었습니다.', 'NOTIFICATION', 'SUCCESS', 0, '2025-03-11 16:05:00', '2025-03-11 16:00:00', 'system', '2025-03-11 16:05:00', 'system', NULL, NULL),
    (public.uuid_generate_v4(), '11111111-0000-0000-0000-000000000012', '이벤트 당첨을 축하드립니다!', 'MESSAGE', 'SUCCESS', 0, '2025-03-12 10:01:00', '2025-03-12 10:00:00', 'admin', '2025-03-12 10:01:00', 'admin', NULL, NULL),
    (public.uuid_generate_v4(), '11111111-0000-0000-0000-000000000013', '알림 전송 재시도 중입니다.', 'NOTIFICATION', 'PENDING', 2, NULL, '2025-03-13 08:30:00', 'system', '2025-03-13 08:35:00', 'system', NULL, NULL),
    (public.uuid_generate_v4(), '11111111-0000-0000-0000-000000000014', '포인트가 적립되었습니다.', 'NOTIFICATION', 'SUCCESS', 0, '2025-03-14 12:01:00', '2025-03-14 12:00:00', 'system', '2025-03-14 12:01:00', 'system', NULL, NULL),
    (public.uuid_generate_v4(), '11111111-0000-0000-0000-000000000015', '상품이 품절되었습니다.', 'NOTIFICATION', 'FAIL', 3, NULL, '2025-03-15 14:00:00', 'system', '2025-03-15 14:10:00', 'system', NULL, NULL),
    (public.uuid_generate_v4(), '11111111-0000-0000-0000-000000000016', '1:1 문의 답변이 등록되었습니다.', 'NOTIFICATION', 'SUCCESS', 0, '2025-03-16 17:31:00', '2025-03-16 17:30:00', 'system', '2025-03-16 17:31:00', 'system', NULL, NULL),
    (public.uuid_generate_v4(), '11111111-0000-0000-0000-000000000017', '긴급 공지: 서버 장애 복구 완료', 'MESSAGE', 'SUCCESS', 1, '2025-03-17 03:11:00', '2025-03-17 03:00:00', 'admin', '2025-03-17 03:11:00', 'admin', NULL, NULL),
    (public.uuid_generate_v4(), '11111111-0000-0000-0000-000000000018', '팔로우한 사용자가 글을 올렸습니다.', 'NOTIFICATION', 'SUCCESS', 0, '2025-03-18 09:01:00', '2025-03-18 09:00:00', 'system', '2025-03-18 09:01:00', 'system', NULL, NULL),
    (public.uuid_generate_v4(), '11111111-0000-0000-0000-000000000019', '구독이 만료 예정입니다.', 'NOTIFICATION', 'PENDING', 0, NULL, '2025-03-19 10:00:00', 'system', '2025-03-19 10:00:00', 'system', NULL, NULL),
    (public.uuid_generate_v4(), '11111111-0000-0000-0000-000000000020', '주문이 취소되었습니다.', 'NOTIFICATION', 'SUCCESS', 0, '2025-03-20 15:31:00', '2025-03-20 15:30:00', 'system', '2025-03-20 15:31:00', 'system', NULL, NULL),
    (public.uuid_generate_v4(), '11111111-0000-0000-0000-000000000001', '보안 로그인 감지 알림입니다.', 'NOTIFICATION', 'SUCCESS', 0, '2025-03-21 22:01:00', '2025-03-21 22:00:00', 'system', '2025-03-21 22:01:00', 'system', NULL, NULL),
    (public.uuid_generate_v4(), '11111111-0000-0000-0000-000000000002', '친구 추천 보상이 지급되었습니다.', 'NOTIFICATION', 'SUCCESS', 0, '2025-03-22 11:01:00', '2025-03-22 11:00:00', 'system', '2025-03-22 11:01:00', 'system', NULL, NULL),
    (public.uuid_generate_v4(), '11111111-0000-0000-0000-000000000003', '전체 공지사항을 확인해주세요.', 'MESSAGE', 'FAIL', 3, NULL, '2025-03-23 09:00:00', 'admin', '2025-03-23 09:15:00', 'admin', NULL, NULL),
    (public.uuid_generate_v4(), '11111111-0000-0000-0000-000000000004', '결제 수단이 변경되었습니다.', 'NOTIFICATION', 'SUCCESS', 0, '2025-03-24 14:01:00', '2025-03-24 14:00:00', 'system', '2025-03-24 14:01:00', 'system', NULL, NULL),
    (public.uuid_generate_v4(), '11111111-0000-0000-0000-000000000005', '새 기능이 출시되었습니다!', 'MESSAGE', 'SUCCESS', 0, '2025-03-25 10:01:00', '2025-03-25 10:00:00', 'admin', '2025-03-25 10:01:00', 'admin', NULL, NULL),
    (public.uuid_generate_v4(), '11111111-0000-0000-0000-000000000006', '월간 리포트가 준비되었습니다.', 'NOTIFICATION', 'PENDING', 1, NULL, '2025-03-26 08:00:00', 'system', '2025-03-26 08:05:00', 'system', NULL, NULL),
    (public.uuid_generate_v4(), '11111111-0000-0000-0000-000000000007', '탈퇴 처리가 완료되었습니다.', 'NOTIFICATION', 'SUCCESS', 0, '2025-03-27 16:01:00', '2025-03-27 16:00:00', 'system', '2025-03-27 16:01:00', 'system', NULL, NULL),
    (public.uuid_generate_v4(), '11111111-0000-0000-0000-000000000008', '테스트 알림 메시지입니다.', 'MESSAGE', 'SUCCESS', 0, '2025-03-28 13:01:00', '2025-03-28 13:00:00', 'system', '2025-03-28 13:01:00', 'system', NULL, NULL),
    (public.uuid_generate_v4(), '11111111-0000-0000-0000-000000000009', '재고 부족 알림: 위시리스트 상품', 'NOTIFICATION', 'SUCCESS', 0, '2025-03-29 10:31:00', '2025-03-29 10:30:00', 'system', '2025-03-29 10:31:00', 'system', NULL, NULL),
    (public.uuid_generate_v4(), '11111111-0000-0000-0000-000000000010', '오늘의 할인 쿠폰이 도착했습니다.', 'MESSAGE', 'SUCCESS', 0, '2025-03-30 08:01:00', '2025-03-30 08:00:00', 'admin', '2025-03-30 08:01:00', 'admin', NULL, NULL);
