package by.afinny.deposit.dto;

import by.afinny.deposit.entity.constant.CardStatus;
import by.afinny.deposit.entity.constant.CurrencyCode;
import by.afinny.deposit.entity.constant.PaymentSystem;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter(AccessLevel.PUBLIC)
@ToString
public class ViewCardDto {

    private UUID cardId;
    private String holderName;
    private CardStatus status;
    private String cardNumber;
    private LocalDate expirationDate;
    private String cardName;
    private PaymentSystem paymentSystem;
    private CurrencyCode currencyCode;
    private BigDecimal cardBalance;
    private UUID accountId;
    private String accountNumber;
}

