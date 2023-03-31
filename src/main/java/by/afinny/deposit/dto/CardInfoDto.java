package by.afinny.deposit.dto;

import by.afinny.deposit.entity.constant.CardStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter(AccessLevel.PUBLIC)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class CardInfoDto {

    private String holderName;
    private CardStatus status;
    private BigDecimal transactionLimit;
}
