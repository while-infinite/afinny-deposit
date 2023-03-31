package by.afinny.deposit.unit.repo;

import by.afinny.deposit.entity.Account;
import by.afinny.deposit.entity.Agreement;
import by.afinny.deposit.entity.Product;
import by.afinny.deposit.entity.constant.CurrencyCode;
import by.afinny.deposit.entity.constant.SchemaName;
import by.afinny.deposit.repository.AccountRepository;
import by.afinny.deposit.repository.AgreementRepository;
import by.afinny.deposit.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Sql(
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
        scripts = {"/schema-h2.sql"}
)
@ActiveProfiles("test")
class AgreementRepositoryTest {

    @Autowired
    private AgreementRepository agreementRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    ProductRepository productRepository;

    private final UUID CLIENT_ID = UUID.randomUUID();

    private Agreement activeAgreement;
    private Agreement notActiveAgreement;
    private Agreement agreement;
    private Account account;
    private Product product;

    @BeforeAll
    void setUp() {
        activeAgreement = Agreement.builder()
                .number("number")
                .interestRate(new BigDecimal("10.00"))
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(1))
                .initialAmount(new BigDecimal("1000000.00"))
                .currentBalance(new BigDecimal("10000.00"))
                .isActive(true)
                .autoRenewal(true)
                .account(account)
                .build();
        notActiveAgreement = Agreement.builder()
                .number("number")
                .interestRate(new BigDecimal("10.00"))
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(1))
                .initialAmount(new BigDecimal("1000000.00"))
                .currentBalance(new BigDecimal("10000.00"))
                .isActive(false)
                .autoRenewal(true)
                .account(account)
                .build();

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

        product = Product.builder()
                .name("product")
                .schemaName(SchemaName.FIXED)
                .isCapitalization(true)
                .currencyCode(CurrencyCode.USD)
                .isActive(true)
                .isRevocable(true)
                .minInterestRate(BigDecimal.ONE)
                .maxInterestRate(BigDecimal.TEN)
                .minDurationMonths(4)
                .maxDurationMonths(8)
                .build();

        agreement = Agreement.builder()
                .number("123456789")
                .account(account)
                .product(product)
                .interestRate(BigDecimal.ZERO)
                .startDate(LocalDateTime.now().minusDays(100))
                .endDate(LocalDateTime.now().plusDays(100))
                .initialAmount(BigDecimal.ONE)
                .currentBalance(BigDecimal.TEN)
                .isActive(true)
                .autoRenewal(false)
                .build();
    }

    @AfterEach
    void cleanUp() {
        productRepository.deleteAll();
        agreementRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("If active agreements exists then return only active agreement's list")
    void findByAccountClientIdAndIsActiveTrue_thenReturnAgreementList() {
        //ARRANGE
        account = accountRepository.save(account);
        activeAgreement.setAccount(account);
        activeAgreement = agreementRepository.save(activeAgreement);
        notActiveAgreement.setAccount(account);
        notActiveAgreement = agreementRepository.save(notActiveAgreement);
        //ACT
        List<Agreement> agreements = agreementRepository.findByAccountClientIdAndIsActiveTrue(account.getClientId());
        //VERIFY
        assertThat(agreements).hasSize(1);
        verifyProductFields(agreements);
    }

    @Test
    @Transactional
    @DisplayName("If agreement with id doesn't exist then return empty")
    void findAgreementByIdAndIsActiveTrue_ifAgreementNotExists_thenReturnEmpty() {
        //ACT
        Optional<Agreement> client = agreementRepository.findAgreementByIdAndIsActiveTrue(UUID.randomUUID());
        //VERIFY
        assertThat(client.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("If agreement with id exists then return the agreement")
    void findAgreementByIdAndIsActiveTrue_ifAgreementExists() {
        //ARRANGE
        Integer productId = productRepository.save(product).getId();
        product.setId(productId);
        UUID accountId = accountRepository.save(account).getId();
        account.setId(accountId);
        UUID agreementId = agreementRepository.save(agreement).getId();
        agreement.setId(agreementId);
        //ACT
        Agreement foundAgreement = agreementRepository
                .findAgreementByIdAndIsActiveTrue(agreement.getId())
                .orElseThrow(() -> new EntityNotFoundException("Agreement with id " + agreement.getId() + " wasn't found"));
        //VERIFY
        verifyClient(foundAgreement);
    }

    private void verifyProductFields(List<Agreement> agreements) {
        Agreement foundActiveAgreement = agreements.get(0);
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(foundActiveAgreement.getId()).isEqualTo(activeAgreement.getId());
            softAssertions.assertThat(foundActiveAgreement.getNumber()).isEqualTo(activeAgreement.getNumber());
            softAssertions.assertThat(foundActiveAgreement.getInterestRate()).isEqualTo(activeAgreement.getInterestRate());
            softAssertions.assertThat(foundActiveAgreement.getInitialAmount()).isEqualTo(activeAgreement.getInitialAmount());
            softAssertions.assertThat(foundActiveAgreement.getCurrentBalance()).isEqualTo(activeAgreement.getCurrentBalance());
            softAssertions.assertThat(foundActiveAgreement.getIsActive()).isEqualTo(activeAgreement.getIsActive());
            softAssertions.assertThat(foundActiveAgreement.getAutoRenewal()).isEqualTo(activeAgreement.getAutoRenewal());
        });
    }

    private void verifyClient(Agreement foundAgreement) {
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(foundAgreement.getAutoRenewal()).isEqualTo(agreement.getAutoRenewal());
            softAssertions.assertThat(foundAgreement.getId()).isEqualTo(agreement.getId());
            softAssertions.assertThat(foundAgreement.getIsActive()).isEqualTo(agreement.getIsActive());
            softAssertions.assertThat(foundAgreement.getNumber()).isEqualTo(agreement.getNumber());
        });
    }
}