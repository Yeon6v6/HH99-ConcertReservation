package kr.hhplus.be.server.api.reservation.application.event;

public record ConcertSeatReservedEvent(Long reservationId, int seatNumber, Status status) {
    public enum Status {
        SEAT_RESERVED,            // 예약 중 (사용자가 좌석을 임시예약한 상태)
        PAYMENT_COMPLETED,        // 결제 완료 (결제 프로세스 성공 후)
        SEAT_PAID                 // 최종 좌석 확정 (트랜잭션 커밋 후)
    }
}
