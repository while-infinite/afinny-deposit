package by.afinny.deposit.dto;

import by.afinny.deposit.entity.constant.CurrencyCode;
import by.afinny.deposit.entity.constant.SchemaName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter(AccessLevel.PUBLIC)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class DepositDto {

    private String cardNumber;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal interestRate;
    private BigDecimal currentBalance;
    private Boolean autoRenewal;
    private String name;
    private CurrencyCode currencyCode;
    private SchemaName schemaName;
    private Boolean isCapitalization;
    private Boolean isRevocable;
}
