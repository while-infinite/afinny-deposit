package by.afinny.deposit.dto.kafka;

import by.afinny.deposit.entity.constant.CardStatus;
import by.afinny.deposit.entity.constant.DigitalWallet;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter(AccessLevel.PUBLIC)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ConsumerNewCardEvent {

    private String accountNumber;
    private String cardNumber;
    private BigDecimal transactionLimit;
    private CardStatus status;
    private LocalDate expirationDate;
    private String holderName;
    private DigitalWallet digitalWallet;
    private Integer cardProductId;
    private BigDecimal balance;
}
