package kr.hhplus.be.server.concertresvsys.sample;

import kr.hhplus.be.server.concertresvsys.sample.dto.ApiErrorResponse;
import kr.hhplus.be.server.concertresvsys.sample.dto.ApiResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sample/concerts")
public class MockConcertController {

    // 예매 가능 날짜 조회
    @GetMapping("/{concertId}/dates")
    public ResponseEntity<ApiResponse<?>> getAvailableDates(
            @PathVariable String concertId,
            @RequestHeader("Authorization") String authorization) {

        // Mock 데이터 생성
        List<String> availableDateList = Arrays.asList("2024-01-01", "2024-01-02", "2024-01-03");

        return ResponseEntity.ok(ApiResponse.builder()
                .code("S")
                .message("예매 가능 날짜 조회 성공")
                .data(Map.of(
                        "concertId", concertId,
                        "availableDates", availableDateList
                ))
                .error(null)
                .build());
    }

    // 특정 날짜의 예매 가능 좌석 조회
    @GetMapping("/{concertId}/seats/available")
    public ResponseEntity<ApiResponse<?>> getAvailableSeats(
            @PathVariable String concertId,
            @RequestBody String date,
            @RequestHeader("Authorization") String authorization) {

        // Mock 데이터 생성
        List<Integer> availableSeatNoList = Arrays.asList(5, 17, 21);

        return ResponseEntity.ok(ApiResponse.builder()
                .code("S")
                .message("예매 가능 좌석 조회 성공")
                .data(Map.of(
                        "concertId", concertId,
                        "date", date,
                        "availableSeats", availableSeatNoList
                ))
                .error(null)
                .build());
    }

    // 좌석 예매 요청
    @PostMapping("/{concertId}/seats/reserve")
    public ResponseEntity<ApiResponse<?>> reserveSeat(
            @PathVariable("concertId") String concertId,
            @RequestHeader("Authorization") String authorization,
            @RequestBody ReservationRequest request) {

        // 예매 실패
        if (request.getSeatId() == null || request.getSeatNo() <= 0) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.builder()
                            .code("E")
                            .message("해당 좌석은 이미 다른 사용자에 의해 예약되었습니다.")
                            .data(null)
                            .error(ApiErrorResponse.builder()
                                    .errorCode("409")
                                    .reason("이미 예약된 좌석(데이터 충돌)")
                                    .build())
                            .build());
        }

        // 예약 만료 시간 계산 (5분 후 / Unix Timestamp로 표현)
        long reservedUntil = Instant.now().plusSeconds(5 * 60).getEpochSecond();

        // Mock 예약 ID 생성
        String reservationId = concertId + request.getSeatId() + request.getSeatNo();

        return ResponseEntity.ok(ApiResponse.builder()
                .code("S")
                .message("예약이 성공적으로 처리되었습니다.")
                .data(Map.of(
                        "reservationId", 987214,
                        "concertId", concertId,
                        "date", request.getDate(),
                        "seatId", request.getSeatId(),
                        "seatNo", request.getSeatNo(),
                        "status", "RESERVED",
                        "reservedUntil", reservedUntil
                ))
                .error(null)
                .build());
    }
}

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
class ReservationRequest {
    private String date;
    private Long seatId;
    private int seatNo;
}
