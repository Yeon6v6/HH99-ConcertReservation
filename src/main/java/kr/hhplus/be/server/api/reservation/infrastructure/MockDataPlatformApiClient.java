package kr.hhplus.be.server.api.reservation.infrastructure;

import kr.hhplus.be.server.api.reservation.application.event.ConcertSeatPaidEvent;
import kr.hhplus.be.server.api.reservation.application.event.ConcertSeatReservedEvent;
import org.springframework.stereotype.Component;

@Component
public class MockDataPlatformApiClient implements DataPlatformApiClient {

    @Override
    public void sendSeatReservationInfo(ConcertSeatReservedEvent event) {
        //
    }

    @Override
    public void sendSeatPaidInfo(ConcertSeatPaidEvent event) {
        //
    }
}
