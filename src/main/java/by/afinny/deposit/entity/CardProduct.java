package by.afinny.deposit.entity;

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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = CardProduct.TABLE_NAME)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter(AccessLevel.PUBLIC)
@ToString
public class CardProduct implements Serializable {

    public static final String TABLE_NAME = "card_product";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Integer id;

    @Column(name = "card_name", nullable = false, length = 30)
    private String cardName;

    @Column(name = "payment_system", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private PaymentSystem paymentSystem;

    @Column(name = "cashback", precision = 6, scale = 4)
    private BigDecimal cashback;

    @Column(name = "co_brand", length = 30)
    @Enumerated(EnumType.STRING)
    private CoBrand coBrand;

    @Column(name = "is_virtual")
    private Boolean isVirtual;

    @Column(name = "premium_status", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private PremiumStatus premiumStatus;

    @Column(name = "service_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal servicePrice;

    @Column(name = "product_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal productPrice;

    @Column(name = "currency_code", nullable = false, length = 3)
    @Enumerated(EnumType.STRING)
    private CurrencyCode currencyCode;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "card_duration")
    private Integer cardDuration;
}
