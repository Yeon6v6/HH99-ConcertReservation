package kr.hhplus.be.server.api.reservation.application.event.handler;

import kr.hhplus.be.server.api.concert.application.service.ConcertService;
import kr.hhplus.be.server.api.reservation.application.event.PaymentFailedEvent;
import kr.hhplus.be.server.api.reservation.application.service.ReservationService;
import kr.hhplus.be.server.api.token.application.service.TokenService;
import kr.hhplus.be.server.api.user.application.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PaymentCompensationEventHandler {

    private final ReservationService reservationService;
    private final UserService userService;
    private final ConcertService concertService;
    private final TokenService tokenService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentFailedEvent(PaymentFailedEvent event){
        // 보상 로직 실행(각 서비스의 보상 method 호출) => 토큰은 처리 안함
        userService.refundPayment(event.getUserId(), event.getPaymentAmount());
        reservationService.cancelResrvation(event.getReservationId());
        concertService.cancelSeatPayment(event.getReservationId());
    }
}
