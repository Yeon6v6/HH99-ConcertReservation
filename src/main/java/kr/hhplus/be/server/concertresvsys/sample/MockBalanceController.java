package kr.hhplus.be.server.concertresvsys.sample;

import kr.hhplus.be.server.concertresvsys.sample.dto.ApiErrorResponse;
import kr.hhplus.be.server.concertresvsys.sample.dto.ApiResponse;
import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/sample")
public class MockBalanceController {

    // Mock 사용자 잔액 데이터
    private final Map<Long, Long> userBalances = new HashMap<>();

    // 잔액 조회 API
    @PostMapping("/users/balance")
    public ResponseEntity<ApiResponse<?>> getBalance(@RequestBody BalanceRequest balanceRequest) {
        Long userId = balanceRequest.getUserId();
        long randomBalance = ThreadLocalRandom.current().nextLong(10000, 1000001);

        return ResponseEntity.ok(ApiResponse.builder()
                .code("S")
                .message("잔액 조회 성공")
                .data(Map.of("userId", userId, "balance", randomBalance))
                .error(null)
                .build());
    }

    // 잔액 충전 API
    @PostMapping("/users/balance/charge")
    public ResponseEntity<ApiResponse<?>>  chargeBalance(@RequestBody BalanceRequest balanceRequest) {
        Long userId = balanceRequest.getUserId();
        Long amount = balanceRequest.getAmount();

        // 잔액 업데이트
        Long currentBalance = userBalances.getOrDefault(userId, 1000L);
        Long newBalance = currentBalance + amount;
        userBalances.put(balanceRequest.getUserId(), newBalance);

        return ResponseEntity.ok(ApiResponse.builder()
                .code("S")
                .message("잔액 충전 성공")
                .data(Map.of("userId", userId, "newBalance", newBalance))
                .error(null)
                .build());
    }

    // 좌석 결제 요청 API
    @PostMapping("/payment")
    public ResponseEntity<ApiResponse<?>> reserveSeat(@RequestBody PaymentRequest paymentRequest) {
        Long userId = paymentRequest.getUserId();
        Long currentBalance = userBalances.getOrDefault(userId, 0L);
        Long useAmount = paymentRequest.getPaymentInfo().getAmount();
        if (currentBalance < useAmount) {
            // 잔액 부족 시 결제 실패 응답
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.builder()
                            .code("E")
                            .message("결제에 실패했습니다.")
                            .data(null)
                            .error(ApiErrorResponse.builder()
                                    .errorCode("409")
                                    .reason("잔액 부족")
                                    .build())
                            .build());
        }

        // 잔액 차감
        Long newBalance = currentBalance - paymentRequest.getPaymentInfo().getAmount();
        userBalances.put(userId, newBalance);

        // 결제 성공 응답
        return ResponseEntity.ok(ApiResponse.builder()
                .code("S")
                .message("결제가 성공적으로 처리되었습니다.")
                .data(Map.of(
                        "paymentId", 8912345L,
                        "reservationId", 132654L,
                        "balance", newBalance
                ))
                .error(null) // 성공의 경우 에러가 없음
                .build());
    }

}

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
class BalanceRequest {
    private Long userId;
    private Long balance; // 현재 금액
    private Long amount;  // 충전 금액
}

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
class PaymentRequest {
    private Long userId;
    private Long reservationId;  // 결제할 예약 ID
    private PaymentInfo paymentInfo; // 결제 정보
}

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
class PaymentInfo {
    private Long amount;       // 결제 금액
    private String method;       // 결제 수단 (예: "CREDIT_CARD", "PAYPAL")
}