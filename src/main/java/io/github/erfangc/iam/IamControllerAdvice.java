package io.github.erfangc.iam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.UUID;

@ControllerAdvice
public class IamControllerAdvice {

    private static final Logger logger = LoggerFactory.getLogger(IamControllerAdvice.class);

    @ExceptionHandler
    public ResponseEntity<ApiError> handle(ApiException e) {
        String id = UUID.randomUUID().toString();
        logger.info("Encountered exception id={}, message={}", id, e.getMessage());
        e.printStackTrace();
        ApiError apiError = new ApiError()
                .setMessage(e.getMessage())
                .setTimestamp(e.getTimestamp().toString());
        return new ResponseEntity<>(apiError, e.getHttpStatus());
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> handleUnexpected(Exception e) {
        String id = UUID.randomUUID().toString();
        logger.info("Unexpected exception id={}, message={}", id, e.getMessage());
        e.printStackTrace();
        ApiError apiError = new ApiError()
                .setId(id)
                .setMessage("An unexpected error has occurred")
                .setTimestamp(Instant.now().toString());
        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
