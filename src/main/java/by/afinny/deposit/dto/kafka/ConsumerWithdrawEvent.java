package by.afinny.deposit.dto.kafka;

import by.afinny.deposit.entity.OperationType;
import by.afinny.deposit.entity.constant.CurrencyCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter(AccessLevel.PUBLIC)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ConsumerWithdrawEvent {

    private UUID agreementId;
    private String accountNumber;
    private Boolean isActive;
    private BigDecimal currentBalance;
    private String completedAt;
    private BigDecimal sum;
    private CurrencyCode currencyCode;
    private OperationType type;
}
