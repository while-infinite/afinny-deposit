package by.afinny.deposit.unit.repo;

import by.afinny.deposit.entity.Account;
import by.afinny.deposit.entity.constant.CurrencyCode;
import by.afinny.deposit.repository.AccountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Sql(
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
        scripts = {"/schema-h2.sql"}
)
@ActiveProfiles("test")
class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    private Account account;

    @BeforeAll
    void setUp() {

        account = Account.builder()
                .accountNumber("0987654321")
                .clientId(UUID.fromString("0d357c23-c7c5-4c3f-8389-d85064f71f76"))
                .currencyCode(CurrencyCode.USD)
                .currentBalance(BigDecimal.TEN)
                .openDate(LocalDate.now().minusMonths(2))
                .closeDate(LocalDate.now().plusMonths(2))
                .isActive(true)
                .blockedSum(BigDecimal.TEN)
                .build();
    }

    @AfterEach
    void cleanUp() {
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("If account with client id and currency code exists then return the account")
    void getAccountsByClientIdAndCurrencyCode() {
        //ARRANGE
        accountRepository.save(account);

        //ACT
        Account foundAccount = accountRepository
                .getAccountsByClientIdAndCurrencyCode(account.getClientId(), account.getCurrencyCode())
                .orElseThrow(() -> new EntityNotFoundException("account with client id " + account.getClientId() + " and currency code " + account.getCurrencyCode() + " wasn't found"));

        //VERIFY
        verifyClient(foundAccount);
    }

    private void verifyClient(Account foundAccount) {
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(foundAccount.getClientId()).isEqualTo(account.getClientId());
            softAssertions.assertThat(foundAccount.getCurrencyCode()).isEqualTo(account.getCurrencyCode());
        });
    }
}