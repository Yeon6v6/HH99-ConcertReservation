package kr.hhplus.be.server.api.reservation.domain.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.api.common.exception.CustomException;
import kr.hhplus.be.server.api.common.type.ReservationStatus;
import kr.hhplus.be.server.api.reservation.exception.ReservationErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long seatId;
    private int seatNumber; // 좌석 번호

    private Long concertId; // 콘서트 ID
    private LocalDate scheduleDate; // 스케줄 날짜

    @Enumerated(EnumType.STRING)
    private ReservationStatus status; // 예약 상태
    private LocalDateTime expiredAt; // (좌석)예약 만료일

    private Long price; // (예약좌석)결제 금액
    private LocalDateTime paidAt; // 결제일

    /**
     * 예약 생성
     */
    public static Reservation create(Long userId, Long seatId, int seatNumber, Long concertId, LocalDate scheduleDate, Long price) {
        LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(5);
        return new Reservation(
                null,
                userId,
                seatId,
                seatNumber,
                concertId,
                scheduleDate,
                ReservationStatus.PENDING,
                expiredAt,
                price,
                null
        );
    }

    /**
     * 결제 관련 내용 업데이트
     * - 예약의 결제 상태 업데이트 및 결제 정보 기록
     */
    public void pay(Long amount) {
        this.status = ReservationStatus.PAID;
        this.price = amount; //예약할 때 지불한 금액을 의미
        this.paidAt = LocalDateTime.now();
    }

    /**
     * 예약 취소
     */
    public void cancel() {
        this.status = ReservationStatus.PENDING;
        this.expiredAt = null;
        this.price = null;
        this.paidAt = null;
    }

    public void validate() {
        if (this.expiredAt.isBefore(LocalDateTime.now())) {
            throw new CustomException(ReservationErrorCode.RESERVATION_EXPIRED);
        }
        if (this.status != ReservationStatus.PENDING) {
            throw new CustomException(ReservationErrorCode.INVALID_RESERVATION_STATUS);
        }
    }
}
