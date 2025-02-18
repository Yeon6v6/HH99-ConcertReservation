package kr.hhplus.be.server.api.reservation.application.event;

import lombok.Getter;

@Getter
public class PaymentFailedEvent {
    private final Long reservationId;
    private final Long userId;
    private final Long paymentAmount;

    public PaymentFailedEvent(Long reservationId, Long userId, Long paymentAmount) {
        this.reservationId = reservationId;
        this.userId = userId;
        this.paymentAmount = paymentAmount;
    }
}
