package kr.hhplus.be.server.api.reservation.application.facade;

import kr.hhplus.be.server.api.common.exception.CustomException;
import kr.hhplus.be.server.api.common.lock.annotation.RedisLock;
import kr.hhplus.be.server.api.concert.application.dto.response.ConcertSeatResult;
import kr.hhplus.be.server.api.concert.application.service.ConcertService;
import kr.hhplus.be.server.api.concert.exception.SeatErrorCode;
import kr.hhplus.be.server.api.reservation.application.dto.command.PaymentCommand;
import kr.hhplus.be.server.api.reservation.application.dto.command.ReservationCommand;
import kr.hhplus.be.server.api.reservation.application.dto.result.PaymentResult;
import kr.hhplus.be.server.api.reservation.application.dto.result.ReservationResult;
import kr.hhplus.be.server.api.reservation.application.event.ConcertSeatPaidEvent;
import kr.hhplus.be.server.api.reservation.application.event.ConcertSeatReservedEvent;
import kr.hhplus.be.server.api.reservation.application.event.PaymentFailedEvent;
import kr.hhplus.be.server.api.reservation.application.service.ReservationService;
import kr.hhplus.be.server.api.reservation.domain.entity.Reservation;
import kr.hhplus.be.server.api.token.application.service.TokenService;
import kr.hhplus.be.server.api.user.application.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationFacade {
    private final ReservationService reservationService;
    private final ConcertService concertService;
    private final UserService userService;
    private final TokenService tokenService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 좌석 예약
     * - ConcertService를 통해 좌석 예약
     * - ReservationService를 통해 예약 정보 생성
     */
    @RedisLock(prefix = "seat:", key = "#reservationCmd.seatId")
//    @Transactional // 예약은 기존 로직대로 처리(필요 시 독립 트랜잭션 적용 예정)
    public ReservationResult reserveSeat(ReservationCommand reservationCmd) {
        // 1. 좌석 상태 확인
        ConcertSeatResult seatResult = concertService.reserveSeat(reservationCmd.seatId());

        if(!seatResult.isAvailable()) {
            throw new CustomException(SeatErrorCode.SEAT_ALREADY_RESERVED);
        }

        // 2. 예약 정보 생성
        ReservationResult reservationResult = reservationService.createReservation(reservationCmd);

        // 예약 생성 이벤트 발행 (AFTER_COMMIT)
        eventPublisher.publishEvent(new ConcertSeatReservedEvent(
                reservationResult.reservationId(),
                reservationCmd.seatNumber(),
                ConcertSeatReservedEvent.Status.SEAT_RESERVED
        ));

        return reservationResult;
    }

    /**
     * 예약된 좌석 결제
     * - userService를 통해 결제 처리
     * - ReservationService를 통해 상태 업데이트
     */
    @RedisLock(prefix = "seat:", key = "#paymentCmd.seatId")
    public PaymentResult payReservation(PaymentCommand paymentCmd) {
        try{
            // 1. 예약 조회 (별도 트랜잭션: ReservationService.findById는 REQUIRES_NEW 적용)
            Reservation reservation = reservationService.findById(paymentCmd.reservationId());
            if (reservation == null) {
                throw new CustomException(SeatErrorCode.SEAT_NOT_RESERVED);
            }

            // 2. 예약 유효성 검증(금액 및 예약상태)
            reservation.validate();
            Long seatPrice = reservation.getPrice();

            // 3. 결제 처리 (및 잔액) - 별도 트랜잭션: UserService.processPayment
            Long remainingBalance = userService.processPayment(paymentCmd.userId(), paymentCmd.paymentAmount());

            // 4. 실제 결제 금액 계산
            Long paidAmount = seatPrice - remainingBalance + paymentCmd.paymentAmount();

            // 5. 좌석 상태 변경 및 일정 확인 - 별도 트랜잭션: ConcertService.payForSeat
            ConcertSeatResult seatResult = concertService.payForSeat(reservation.getSeatId());

            // 6. 예약 상태 및 결제 정보 업데이트 - 별도 트랜잭션: ReservationService.updateReservation
            reservation.pay(paidAmount);
            reservationService.updateReservation(reservation);

            // 7. 대기열 토큰 만료 처리 - 별도 트랜잭션: TokenService.getTokenIdByUserId / expireToken
            Long tokenId = tokenService.getTokenIdByUserId(reservation.getUserId());
            if (tokenId != null) {
                tokenService.expireToken(tokenId);
            }

            // 8. 좌석 결제 완료 이벤트 발행
            eventPublisher.publishEvent(new ConcertSeatPaidEvent(
                    reservation.getId(),
                    reservation.getConcertId(),
                    reservation.getSeatNumber(),
                    reservation.getUserId(),
                    seatPrice,
                    paidAmount
            ));

            // 9. PaymentResult 생성 및 반환
            return new PaymentResult(
                    reservation.getId(),
                    seatResult.status(),
                    remainingBalance,
                    seatPrice,
                    paidAmount,
                    reservation.getPaidAt()
            );
        }catch(Exception ex){
            // 실패 발생 시 보상 이벤트 발행
            eventPublisher.publishEvent(new PaymentFailedEvent(
                    paymentCmd.reservationId(),
                    paymentCmd.userId(),
                    paymentCmd.paymentAmount()
            ));
            throw ex;
        }
    }
}