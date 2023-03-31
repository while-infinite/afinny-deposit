package by.afinny.deposit.dto;

import by.afinny.deposit.entity.constant.CoBrand;
import by.afinny.deposit.entity.constant.CurrencyCode;
import by.afinny.deposit.entity.constant.PaymentSystem;
import by.afinny.deposit.entity.constant.PremiumStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter(AccessLevel.PUBLIC)
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardProductDto implements Serializable {

    private Integer id;
    private String cardName;
    private PaymentSystem paymentSystem;
    private BigDecimal cashback;
    private CoBrand coBrand;
    private Boolean isVirtual;
    private PremiumStatus premiumStatus;
    private BigDecimal servicePrice;
    private BigDecimal productPrice;
    private CurrencyCode currencyCode;
    private Boolean isActive;
    private Integer cardDuration;
}
