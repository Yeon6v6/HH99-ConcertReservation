package kr.hhplus.be.server.api.reservation.application.event.listener;

import kr.hhplus.be.server.api.reservation.application.event.ConcertSeatPaidEvent;
import kr.hhplus.be.server.api.reservation.application.event.ConcertSeatReservedEvent;
import kr.hhplus.be.server.api.reservation.infrastructure.DataPlatformApiClient;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ReservationEventListener {
    private static final Logger log = LoggerFactory.getLogger(ReservationEventListener.class);
    private final DataPlatformApiClient dataPlatformApiClient;

    /**
     * 좌석 예약 시 데이터 플랫폼에 전달
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendReservedSeatInfo(ConcertSeatReservedEvent event) {
        try{
            dataPlatformApiClient.sendSeatReservationInfo(event);
            System.out.println("reservation event");
        }catch (Exception e){
            log.error("Failed to send reservation seat info: reservationId={}, seatId={}, eventStatus={}, error={}",
                    event.reservationId(), event.seatNumber(), event.status(), e.getMessage(), e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendPaidSeatInfo(ConcertSeatPaidEvent event) {
        try {
            dataPlatformApiClient.sendSeatPaidInfo(event);
            System.out.println("paid event");
        }catch (Exception e){
            log.error("Failed to send reservation payment info: reservationId={}, concertId={}, seatNumber={}, userId={}, seatPrice={}, finalPrice={}, error={}",
                    event.reservationId(), event.concertId(), event.seatNumber(), event.userId(), event.seatPrice(), event.finalPrice(), e.getMessage(), e);
        }
    }
}
