package kr.hhplus.be.server.api.reservation.infrastructure;

import kr.hhplus.be.server.api.reservation.application.event.ConcertSeatPaidEvent;
import kr.hhplus.be.server.api.reservation.application.event.ConcertSeatReservedEvent;

public interface DataPlatformApiClient {
    void sendSeatReservationInfo(ConcertSeatReservedEvent event);
    void sendSeatPaidInfo(ConcertSeatPaidEvent event);
}
