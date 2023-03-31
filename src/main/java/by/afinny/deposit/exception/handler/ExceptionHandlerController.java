package by.afinny.deposit.exception.handler;

import by.afinny.deposit.exception.CardStatusesAreEqualsException;
import by.afinny.deposit.exception.DefaultCardNotFoundException;
import by.afinny.deposit.exception.dto.ErrorDto;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import by.afinny.deposit.exception.EntityNotFoundException;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@ControllerAdvice
@Slf4j
public class ExceptionHandlerController {

    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> serverExceptionHandler(Exception e) {
        log.error("Internal server error. " + e.getMessage());
        return createResponseEntity(
                HttpStatus.INTERNAL_SERVER_ERROR,
                new ErrorDto(Integer.toString(HttpStatus.INTERNAL_SERVER_ERROR.value()),
                        "Internal server error"));
    }

    @ExceptionHandler(CardStatusesAreEqualsException.class)
    public ResponseEntity<ErrorDto> cardStatusesAreEqualsExceptionHandler(CardStatusesAreEqualsException e) {
        log.error("Statuses are the same. " + e.getMessage());
        return createResponseEntity(
                HttpStatus.BAD_REQUEST,
                new ErrorDto(e.getErrorCode(),
                        e.getErrorMessage()));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorDto> entityNotFoundExceptionHandler(EntityNotFoundException e) {
        log.error("Entity not found. " + e.getMessage());
        return createResponseEntity(
                HttpStatus.BAD_REQUEST,
                new ErrorDto(Integer.toString(HttpStatus.BAD_REQUEST.value()),
                        e.getMessage()));
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<String> feignExceptionHandler(FeignException exception) {
        ResponseEntity.BodyBuilder responseBuilder = getDefaultResponseEntityBuilder(exception.status());

        Optional<ByteBuffer> body = exception.responseBody();
        if (body.isPresent()) {
            String message = getDecodedResponseBody(body.get());
            return responseBuilder.body(message);
        }

        return responseBuilder.build();
    }

    @ExceptionHandler(DefaultCardNotFoundException.class)
    ResponseEntity<Void> defaultCardNotFoundExceptionHandler() {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ExceptionHandler(CardExpiredException.class)
    public ResponseEntity<ErrorDto> cardExpiredException(CardExpiredException e) {
        return createResponseEntity(
                HttpStatus.BAD_REQUEST,
                new ErrorDto(Integer.toString(HttpStatus.BAD_REQUEST.value()),
                        e.getMessage()));
    }

    private ResponseEntity<ErrorDto> createResponseEntity(HttpStatus status, ErrorDto errorDto) {
        return ResponseEntity.status(status)
                .header("Content-Type", "application/json")
                .body(errorDto);
    }

    private ResponseEntity.BodyBuilder getDefaultResponseEntityBuilder(int status) {
        return ResponseEntity.status(HttpStatus.valueOf(status)).contentType(MediaType.APPLICATION_JSON);
    }

    private String getDecodedResponseBody(ByteBuffer byteBuffer) {
        return StandardCharsets.UTF_8.decode(byteBuffer).toString();
    }
}
