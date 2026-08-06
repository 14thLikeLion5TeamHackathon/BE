package likelion.madi.common.exception;

import org.springframework.http.HttpStatus;

import likelion.madi.common.response.ErrorStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BaseException extends RuntimeException {
    @Getter(AccessLevel.NONE)
    HttpStatus statusCode;
    String errorCode;
    String responseMessage;

    public BaseException(HttpStatus statusCode) {
        super();
        this.statusCode = statusCode;
    }

    public BaseException(HttpStatus statusCode, String responseMessage) {
        super();
        this.statusCode = statusCode;
        this.responseMessage = responseMessage;
    }

    public BaseException(ErrorStatus errorStatus) {
        super();
        this.statusCode = errorStatus.getHttpStatus();
        this.errorCode = errorStatus.getErrorCode();
        this.responseMessage = errorStatus.getMessage();
    }

    public int getStatusCode() {
        return this.statusCode.value();
    }
}