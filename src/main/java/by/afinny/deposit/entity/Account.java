package by.afinny.deposit.entity;

import by.afinny.deposit.entity.constant.CurrencyCode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = Account.TABLE_NAME)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter(AccessLevel.PUBLIC)
@ToString
public class Account {

    public static final String TABLE_NAME = "account";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "client_id", nullable = false)
    private UUID clientId;

    @Column(name = "current_balance", nullable = false)
    private BigDecimal currentBalance;

    @Column(name = "open_date", nullable = false)
    private LocalDate openDate;

    @Column(name = "close_date", nullable = false)
    private LocalDate closeDate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "salary_project")
    private String salaryProject;

    @Column(name = "currency_code", nullable = false)
    @Enumerated(EnumType.STRING)
    private CurrencyCode currencyCode;

    @Column(name = "blocked_sum", nullable = false, precision = 19, scale = 4)
    private BigDecimal blockedSum;

    @OneToMany(mappedBy = "account", cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @Fetch(value = FetchMode.SUBSELECT)
    @ToString.Exclude
    @JsonIgnore
    private List<Card> cards;

    @OneToMany(mappedBy = "account", cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @Fetch(value = FetchMode.SUBSELECT)
    @ToString.Exclude
    @JsonIgnore
    private List<Operation> operations;

    @OneToMany(mappedBy = "account", cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @Fetch(value = FetchMode.SUBSELECT)
    @ToString.Exclude
    @JsonIgnore
    private List<Agreement> agreements;

    @PrePersist
    private void onCreate() {
        changeBalance();
    }

    @PreUpdate
    private void onUpdate() {
        changeBalance();
    }
    private void changeBalance(){
        if (cards != null) {
            this.currentBalance = cards.stream()
                    .map(Card::getBalance)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }
}
