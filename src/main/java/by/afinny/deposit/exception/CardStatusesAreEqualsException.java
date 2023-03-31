package by.afinny.deposit.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter(AccessLevel.PUBLIC)
@RequiredArgsConstructor
public class CardStatusesAreEqualsException extends RuntimeException {

    private final String errorCode;
    private final String errorMessage;
}