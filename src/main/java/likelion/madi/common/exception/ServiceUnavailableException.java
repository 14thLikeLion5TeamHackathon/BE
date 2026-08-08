package likelion.madi.common.exception;

import org.springframework.http.HttpStatus;

import likelion.madi.common.response.ErrorStatus;

public class ServiceUnavailableException extends BaseException {
    public ServiceUnavailableException() {
        super(HttpStatus.SERVICE_UNAVAILABLE);
    }

    public ServiceUnavailableException(String message) {
        super(HttpStatus.SERVICE_UNAVAILABLE, message);
    }

    public ServiceUnavailableException(ErrorStatus errorStatus) {
        super(errorStatus);
    }
}