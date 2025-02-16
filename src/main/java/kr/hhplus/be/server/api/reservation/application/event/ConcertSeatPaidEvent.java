package kr.hhplus.be.server.api.reservation.application.event;

public record ConcertSeatPaidEvent(Long reservationId, Long concertId, int seatNumber, Long userId, Long seatPrice, Long finalPrice) {}
