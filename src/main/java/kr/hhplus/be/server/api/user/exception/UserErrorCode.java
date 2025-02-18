package kr.hhplus.be.server.api.user.exception;

import kr.hhplus.be.server.api.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum UserErrorCode implements ErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자 정보를 찾을 수 없습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String name;
    private final String message;

    UserErrorCode(HttpStatus httpStatus, String name, String message) {
        this.httpStatus = httpStatus;
        this.name = name;
        this.message = message;
    }
}