package likelion.madi.common.exception;

import org.springframework.http.HttpStatus;

import likelion.madi.common.response.ErrorStatus;

public class ForbiddenException extends BaseException {
    public ForbiddenException() {
        super(HttpStatus.FORBIDDEN);
    }

    public ForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }

    public ForbiddenException(ErrorStatus errorStatus) {
        super(errorStatus);
    }
}