package by.afinny.deposit.dto;

import by.afinny.deposit.entity.constant.CardStatus;
import by.afinny.deposit.entity.constant.DigitalWallet;
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
public class CardDto {

    private UUID cardId;
    private String cardNumber;
    private BigDecimal transactionLimit;
    private CardStatus status;
    private LocalDate expirationDate;
    private String holderName;
    private DigitalWallet digitalWallet;
    private Boolean isDefault;
    private Integer cardProductId;
    private String cardName;
    private PaymentSystem paymentSystem;
}
