package by.afinny.deposit.dto.userservice;

import by.afinny.deposit.entity.constant.CurrencyCode;
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
public class AccountDto {

    private String accountNumber;
    private UUID clientId;
    private BigDecimal currentBalance;
    private LocalDate openDate;
    private LocalDate closeDate;
    private Boolean isActive;
    private String salaryProject;
    private CurrencyCode currencyCode;
}
