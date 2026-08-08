package likelion.madi.common.exception;

import org.springframework.http.HttpStatus;

import likelion.madi.common.response.ErrorStatus;

public class InternalServerException extends BaseException {
    public InternalServerException() {
        super(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public InternalServerException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    public InternalServerException(ErrorStatus errorStatus) {
        super(errorStatus);
    }
}