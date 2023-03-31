package by.afinny.deposit.entity;

import by.afinny.deposit.entity.constant.CurrencyCode;
import by.afinny.deposit.entity.constant.SchemaName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = Product.TABLE_NAME)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter(AccessLevel.PUBLIC)
@ToString
public class Product implements Serializable {

    public static final String TABLE_NAME = "product";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "interest_rate_early")
    private BigDecimal interestRateEarly;

    @Column(name = "is_capitalization", nullable = false)
    private Boolean isCapitalization;

    @Column(name = "amount_min")
    private BigDecimal amountMin;

    @Column(name = "amount_max")
    private BigDecimal amountMax;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "is_revocable", nullable = false)
    private Boolean isRevocable;

    @Column(name = "min_interest_rate", nullable = false)
    private BigDecimal minInterestRate;

    @Column(name = "max_interest_rate", nullable = false)
    private BigDecimal maxInterestRate;

    @Column(name = "min_duration_months", nullable = false)
    private Integer minDurationMonths;

    @Column(name = "max_duration_months", nullable = false)
    private Integer maxDurationMonths;

    @Enumerated(EnumType.STRING)
    @Column(name = "schema_name", nullable = false)
    private SchemaName schemaName;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency_code", nullable = false)
    private CurrencyCode currencyCode;

    @OneToMany(mappedBy = "product", cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REMOVE})
    @JsonIgnore
    @ToString.Exclude
    private List<Agreement> agreement;

    @Column(name = "active_since")
    private Instant activeSince;

    @Column(name = "active_until")
    private Instant activeUntil;
}
