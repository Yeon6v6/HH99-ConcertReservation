# 동시성 이슈 및 제어 방식 분석

1\. 동시성 이슈 발생 사례

* **좌석 예약**:
  * 여러 사용자가 동일한 좌석을 동시에 예약하려고 시도할 경우
    * 특정 시간(5분) 동안 좌석이 최초 요청사용자에게 임시 배정 상태로 유지되어야 하며, 해당 시간 동안 다른 사용자가 동일 좌석에 대한 접근(수정) 불가
  *

      ```java
      /**
       * 좌석 예약
       * - ConcertService를 통해 좌석 예약
       * - ReservationService를 통해 예약 정보 생성
       */
      @Transactional
      public ReservationResult reserveSeat(ReservationCommand reservationCmd) {
          // 1. 좌석 상태 확인
          ConcertSeatResult seatResult = concertService.reserveSeat(reservationCmd.seatId());

          // 2. 예약 정보 생성
          return reservationService.createReservation(reservationCmd);
      }
      ```
* **포인트 충전 및 사용**:
  * 한 사용자가 동시에 여러 요청을 보내는 경우, 잔액 불일치와 같은 데이터 무결성 문제 발생

***

## 2. 동시성 제어 방식

### **2.1 Redis 분산 락**

_Redis를 이용해 락을 관리 / TTL(Timeout)을 설정하여 데드락을 방지_

* **장점**:
  * 다중 서버 환경에서 일관된 락 관리 가능
  * 메모리 기반으로 성능이 빠름
  * TTL로 데드락 방지
* **단점**:
  * Redis 장애 시 락 관리 불가
  * 락 만료 시간과 실제 작업 시간이 불일치 할 경우 문제 발생 가능
  * 구현 복잡도 증가(복구 로직 필요)
* **적합한 경우**:
  * **좌석 예약**: 경쟁이 심한 VIP 좌석과 같은 리소스
  * **포인트 충전 및 사용**: 사용자별 키로 동시성 충돌 방지
* **Redis 락 세부 구현 방법**:
  1. **Simple Lock**: `SETNX` 명령을 사용하여 간단하게 락 설정
     * 장점: 구현이  간단하며, 빠른 락 획득
     * 단점: 만료 시간이 지나기 전에 프로세스가 완료되지 않으면 락 해제가 불완전할 수 있음
  2. **Pub/Sub 기반 락**: 락 해제 이벤트를 다른 프로세스에 알리기 위해 Pub/Sub 사용
     * 장점: 이벤트 기반으로 락 상태를 관리, 대기 중인 프로세스가 즉시 반응 가능
     * 단점: Pub/Sub 구현이 추가되므로 복잡도 증가
  3. **Redlock**: Redis 클러스터 환경에서 여러 노드에 락을 걸어 분산 환경에서도 강력한 동시성 제어
     * 장점: 다중 Redis 노드 지원
     * 단점: 다중 노드 환경에서 복잡한 구현 및 추가 설정 필요
  4. **Spin Lock**: 락을 획득할 때까지 루프를 돌며 지속적으로 시도
     * 장점: 락 대기 시간이 짧을 경우 빠른 락 획득 가능
     * 단점: 대기 시간이 길어지면 CPU 점유율 상승

### **2.2 비관적 락**

_데이터베이스에서 트랜잭션 단위로 락을 설정하여 다른 트랜잭션 접근 차단_

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")}) //동시성이 많을 수 있으므로 락 대기시간 설정
Optional<Seat> findByIdWithLock(@Param("seatId") Long seatId);
```

* **장점**:
  * 데이터베이스 수준에서의 일관성 제공
  * 구현이 간단하며 외부 의존성 없음
* **단점**:
  * 경쟁이 많은 경우 트랜잭션 대기로 인해 병목 가능성 있음음
  * 단일 데이터베이스 환경에서만 효과적
* **적합한 경우**:
  * **좌석 예약**: 단일 데이터베이스 환경에서 좌석 충돌 방지가 필요한 경우

### **2.3 낙관적 락**

_데이터 변경 시 충돌을 검출하고 롤백을 통해 충돌을 해결(조회 시에는 락 X)_

```java
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;
}
```

* **장점**:
  * 충돌이 적은 경우 성능 최적화 가능
  * 확장성이 뛰어나며 다중 서버에서도 적합
* **단점**:
  * 충돌 발생 시 롤백 및 재시도로 인해 지연 발생
  * 불필요한 작업이 실행될 가능성이 있음음
* **적합한 경우**:
  * **좌석 예약**: 충돌 가능성이 낮은 일반 좌석
  * **포인트 충전 및 사용**: 충돌 빈도가 낮은 상황에서 적합

***

## 3. 최종 선택한 동시성 제어 방식

* **좌석 예약**:
  * **Redis 분산 락 중 Simple Lock**
    * 좌석 예약의 경우 하나만 유효하기 때문에, 첫요청만 처리하고 이후의 요청은 모두 무시해야 하는 상황에서 간단히 해결 가능\
      ⇒ 💡낙관적 락의 경우도 해당 될  수 있으나, 낙관적 락의 경우 실제로 좌석 예약을 시도한 후에야 충돌 여부를 판단할 수 있어 불필요한 데이터베이스 접근과 자원 소모가 발생할 가능성이 있다고 판단하여 Redis를 이용하여 '접근 시도 시' 거절되도록  선택
    * 트랜잭션이나 복잡한 상태 관리가 필요 없고, 일정 시간 후 자동 해제가 가능하므로 관리 비용이 감소

_분석 시에는 좌석에 대한 등급이 있다고 가정했지만, 실제 구현에는 동일한 등급이라고 가정함_

<figure><img src="../../.gitbook/assets/Redis Simple Lock (1).png" alt=""><figcaption></figcaption></figure>

* **포인트 충전 및 사용**:
  * **Redis 분산 락 중 Pub/Sub**:
    * 동일한 채널을 여러 Subscribe가 구독하면서, 메시지가 각각의 Subcriber로 전달되어 순서대로 처리가 되지 않을 수 있으나 Redis Lock으로 각 사용자에 대한 순서는 제어가 가능하며, \
      충전 및 차감 메시지는 각각 독립적인 작업으로 처리되기도 하고 여러 사용자의 요청을 분산 처리 할 수 있기 때문에 선택
    * 충전 이벤트를 구독한 프로세스들이 즉시 업데이트를 반영할 수 있어 데이터 일관성과 실시간성이 증가

_아래의 흐름은 한 사용자에 대한 여러 요청을 기반으로 구현했지만, 실제로는 여러 사람의 여러 요청이 아래와 같은 형태로 병렬처리 됨_

<figure><img src="../../.gitbook/assets/Redis PubSub.png" alt=""><figcaption></figcaption></figure>
