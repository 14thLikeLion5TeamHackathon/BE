package likelion.madi.common.exception;

import likelion.madi.common.response.ErrorStatus;
import org.springframework.http.HttpStatus;

public class ConflictException extends BaseException{
    public ConflictException() {
        super(HttpStatus.CONFLICT);
    }

    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
    public ConflictException(ErrorStatus errorStatus) {
        super(errorStatus);
    }
}
