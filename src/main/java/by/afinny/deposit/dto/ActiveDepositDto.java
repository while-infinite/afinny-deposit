package by.afinny.deposit.dto;

import by.afinny.deposit.entity.constant.CurrencyCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter(AccessLevel.PUBLIC)
@ToString
public class ActiveDepositDto {

    private UUID agreementId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal currentBalance;
    private String productName;
    private CurrencyCode currencyCode;
    private String cardNumber;
}
