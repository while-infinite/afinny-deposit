package by.afinny.deposit.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultCardNotFoundException extends RuntimeException {

    public DefaultCardNotFoundException(String message) {
        super(message);
        log.error("Ex: {} ", message);
    }
}
