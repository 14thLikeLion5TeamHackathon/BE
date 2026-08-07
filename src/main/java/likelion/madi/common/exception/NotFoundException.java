package likelion.madi.common.exception;

import likelion.madi.common.response.ErrorStatus;
import org.springframework.http.HttpStatus;

public class NotFoundException extends BaseException {
    public NotFoundException() {
        super(HttpStatus.NOT_FOUND);
    }

    public NotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
    public NotFoundException(ErrorStatus errorStatus) {
        super(errorStatus);
    }
}
