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

import java.io.Serializable;
import java.math.BigDecimal;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter(AccessLevel.PUBLIC)
@ToString
public class ProductDto implements Serializable {

    private String name;
    private Integer id;
    private BigDecimal minInterestRate;
    private BigDecimal maxInterestRate;
    private BigDecimal interestRateEarly;
    private CurrencyCode currencyCode;
    private Boolean isRevocable;
    private SchemaName schemaName;
    private Boolean isCapitalization;
    private Integer minDurationMonths;
    private Integer maxDurationMonths;
    private BigDecimal amountMin;
    private BigDecimal amountMax;
}
