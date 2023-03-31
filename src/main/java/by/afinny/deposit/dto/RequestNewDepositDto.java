package by.afinny.deposit.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter(AccessLevel.PUBLIC)
@ToString
public class RequestNewDepositDto {

    private Integer productId;
    private BigDecimal initialAmount;
    private String cardNumber;
    private Boolean autoRenewal;
    private BigDecimal interestRate;
    private Integer durationMonth;
}
