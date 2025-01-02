package kr.hhplus.be.server.concertresvsys.sample.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiResponse<T> {
    private String code;        // "S" (성공) 또는 "E" (실패)
    private String message;     // 응답 메시지
    private T data;             // 성공 시 데이터 (제네릭 타입)
    private ApiErrorResponse error; // 실패 시 에러 정보
}

