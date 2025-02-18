package kr.hhplus.be.server.api.reservation.application.service;

import kr.hhplus.be.server.api.common.exception.CustomException;
import kr.hhplus.be.server.api.reservation.application.dto.command.ReservationCommand;
import kr.hhplus.be.server.api.reservation.application.dto.result.ReservationResult;
import kr.hhplus.be.server.api.reservation.application.factory.ReservationResultFactory;
import kr.hhplus.be.server.api.reservation.domain.entity.Reservation;
import kr.hhplus.be.server.api.reservation.domain.factory.ReservationFactory;
import kr.hhplus.be.server.api.reservation.domain.repository.ReservationRepository;
import kr.hhplus.be.server.api.reservation.exception.ReservationErrorCode;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);

    private final ReservationRepository reservationRepository;
    private final ReservationFactory reservationFactory;
    private final ReservationResultFactory reservationResultFactory;

    /**
     * 예약 ID로 예약 정보 조회
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Reservation findById(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ReservationErrorCode.RESERVATION_NOT_FOUND));
    }

    /**
     * 예약 생성(좌석 예약)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ReservationResult createReservation(ReservationCommand command) {
        log.info("[ReservationService] 예약 생성 시작 >> User ID: {}, Seat ID: {}", command.userId(), command.seatId());

        // 비관적 락으로 좌석 상태 확인
        Reservation existingReservation = reservationRepository.findBySeatIdWithLock(command.seatId());
        if (existingReservation != null) {
            // 예약 만료 여부 확인
            if (existingReservation.getExpiredAt().isAfter(LocalDateTime.now())) {
                throw new CustomException(ReservationErrorCode.RESERVATION_ALREADY_EXISTS);
            }
        }

        try {
            // 예약(Reservation 객체) 생성
            Reservation reservation = reservationFactory.createReservation(command);
            Reservation savedReservation = reservationRepository.save(reservation);
            log.info("[ReservationService] 예약 생성 완료 >> Reservation ID: {}", reservation.getId());

            return reservationResultFactory.createResult(savedReservation);
        } catch (CustomException e) {
            log.error("[ReservationService] 예약 생성 실패 >> User ID: {}, Seat ID: {}", command.userId(), command.seatId(), e);
            throw e;
        }
    }
    
    /**
     * 예약 정보 업데이트
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateReservation(Reservation reservation) {
        reservationRepository.save(reservation);
    }

    /**
     * 좌석에 대한 모든 예약 조회
     */
    public List<Reservation> findAllReservationsBySeatId(Long seatId) {
        return reservationRepository.findBySeatId(seatId);
    }

    /**
     * 결제 업데이트 취소에 따른 예약 정보 업데이트
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void cancelResrvation(Long reservationId) {
        Reservation reservation = findById(reservationId);
        if(reservation != null) {
            reservation.cancel();
            updateReservation(reservation);
        }
    }
}
