-- ------------------------------------------------------------
-- 1. USER 테이블 데이터 삽입
-- ------------------------------------------------------------
INSERT INTO user (balance) VALUES
                               (5000),
                               (10000),
                               (7500),
                               (0),
                               (2000);

-- 추가 1000명의 사용자 데이터 삽입
INSERT INTO user (balance)
SELECT FLOOR(RAND() * 10000)
FROM (
         SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
         UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10
     ) a,
     (
         SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
         UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10
     ) b,
     (
         SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
         UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10
     ) c
LIMIT 1000;

-- ------------------------------------------------------------
-- 2. CONCERT 테이블 데이터 삽입 (1000개 콘서트)
-- ------------------------------------------------------------
INSERT INTO concert ()
SELECT NULL
FROM (
         SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
         UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10
     ) a,
     (
         SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
         UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10
     ) b,
     (
         SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
         UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10
     ) c
LIMIT 1000;

-- ------------------------------------------------------------
-- 3. CONCERT_SCHEDULE 테이블 데이터 삽입
-- 각 콘서트당 5개의 스케줄을 생성 (날짜는 2025-01-01부터 순차적으로)
-- ------------------------------------------------------------
INSERT INTO concert_schedule (concert_id, schedule_date, is_sold_out)
SELECT
    c.id,
    DATE_ADD('2025-01-01', INTERVAL FLOOR(RAND()*365) DAY) AS schedule_date,
    RAND() > 0.5 AS is_sold_out
FROM concert c
         CROSS JOIN (
    SELECT 1 AS seq UNION ALL
    SELECT 2 UNION ALL
    SELECT 3 UNION ALL
    SELECT 4 UNION ALL
    SELECT 5
) seq
WHERE seq.seq <= 3 OR (seq.seq > 3 AND RAND() < 0.5);


-- ------------------------------------------------------------
-- 4. SEAT 테이블 데이터 삽입
-- 각 콘서트 스케줄마다 30개의 좌석을 생성 (WITH RECURSIVE 사용)
-- ------------------------------------------------------------
INSERT INTO seat (seat_number, concert_id, schedule_date, status, price)
SELECT
    numbers.n,
    cs.concert_id,
    cs.schedule_date,
    'AVAILABLE',
    50000
FROM concert_schedule cs
         CROSS JOIN (
    SELECT 1 AS n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
    UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10
    UNION ALL SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15
    UNION ALL SELECT 16 UNION ALL SELECT 17 UNION ALL SELECT 18 UNION ALL SELECT 19 UNION ALL SELECT 20
    UNION ALL SELECT 21 UNION ALL SELECT 22 UNION ALL SELECT 23 UNION ALL SELECT 24 UNION ALL SELECT 25
    UNION ALL SELECT 26 UNION ALL SELECT 27 UNION ALL SELECT 28 UNION ALL SELECT 29 UNION ALL SELECT 30
) AS numbers;
;

-- Get-Content -Path ".\data\initdb\1_bulk_data.sql" -Raw | docker exec -i concertreservesystem-mysql-1 mysql -u application -papplication hhplus
