package kr.hhplus.be.server.concertresvsys.sample.dto;

import lombok.*;

@Getter
@AllArgsConstructor
@Builder
public class ApiErrorResponse {
    private String errorCode;   // HTTP 상태 코드
    private String reason;      // 에러 사유
}