package by.afinny.deposit.integration.service;

import by.afinny.deposit.dto.AutoRenewalDto;
import by.afinny.deposit.dto.WithdrawDepositDto;
import by.afinny.deposit.dto.kafka.ConsumerWithdrawEvent;
import by.afinny.deposit.entity.Account;
import by.afinny.deposit.entity.Agreement;
import by.afinny.deposit.entity.Card;
import by.afinny.deposit.entity.CardProduct;
import by.afinny.deposit.entity.Operation;
import by.afinny.deposit.entity.Product;
import by.afinny.deposit.entity.constant.CardStatus;
import by.afinny.deposit.entity.constant.CoBrand;
import by.afinny.deposit.entity.constant.CurrencyCode;
import by.afinny.deposit.entity.constant.DigitalWallet;
import by.afinny.deposit.entity.constant.PaymentSystem;
import by.afinny.deposit.entity.constant.PremiumStatus;
import by.afinny.deposit.entity.constant.SchemaName;
import by.afinny.deposit.integration.config.annotation.TestWithPostgresContainer;
import by.afinny.deposit.repository.AccountRepository;
import by.afinny.deposit.repository.AgreementRepository;
import by.afinny.deposit.repository.CardProductRepository;
import by.afinny.deposit.repository.CardRepository;
import by.afinny.deposit.repository.OperationRepository;
import by.afinny.deposit.repository.ProductRepository;
import by.afinny.deposit.service.AgreementService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SuppressWarnings("FieldCanBeLocal")
@TestWithPostgresContainer
@Sql(scripts = "/truncate.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("AgreementServiceIT")
public class AgreementServiceImplIT {
    @SpyBean
    private OperationRepository operationRepository;

    private final String ACCOUNT_NUMBER = "1";
    private final String CARD_NUMBER = "123-122-123-124 ";
    private WithdrawDepositDto withdrawDepositDto;
    private AutoRenewalDto autoRenewalDto;
    private ConsumerWithdrawEvent consumerWithdrawEvent;
    private final UUID CLIENT_ID = UUID.randomUUID();
    private Agreement agreement;
    private Product product;
    private Account account;
    private Card card;
    private CardProduct cardProduct;
    @SpyBean
    private AgreementService agreementService;
    @SpyBean
    private AgreementRepository agreementRepository;
    @SpyBean
    private ProductRepository productRepository;
    @SpyBean
    private AccountRepository accountRepository;
    @SpyBean
    private CardProductRepository cardProductRepository;
    @SpyBean
    private CardRepository cardRepository;

    @BeforeAll
    void setUp() {
        withdrawDepositDto = WithdrawDepositDto
                .builder()
                .cardNumber(CARD_NUMBER)
                .build();

        autoRenewalDto = AutoRenewalDto
                .builder()
                .autoRenewal(true)
                .build();
    }

    @BeforeEach
    void save() {
        product = Product.builder()
                .name("Product #1")
                .minInterestRate(new BigDecimal(10))
                .maxInterestRate(new BigDecimal(12))
                .interestRateEarly(new BigDecimal(9))
                .currencyCode(CurrencyCode.RUB)
                .isActive(true)
                .isRevocable(true)
                .isCapitalization(true)
                .schemaName(SchemaName.FIXED)
                .minDurationMonths(10)
                .maxDurationMonths(20)
                .amountMin(new BigDecimal(1))
                .amountMax(new BigDecimal(100000))
                .build();

        account = Account
                .builder()
                .accountNumber(ACCOUNT_NUMBER)
                .clientId(CLIENT_ID)
                .currentBalance(new BigDecimal(100))
                .openDate(LocalDate.of(2020, 1, 15))
                .closeDate(LocalDate.of(2030, 1, 15))
                .isActive(true)
                .salaryProject("salaryProject")
                .currencyCode(CurrencyCode.RUB)
                .blockedSum(new BigDecimal(10))
                .build();

        card = Card.builder()
                .cardNumber(CARD_NUMBER)
                .transactionLimit(new BigDecimal("100"))
                .expirationDate(LocalDate.now().plusDays(1))
                .holderName("Peter Parker")
                .status(CardStatus.ACTIVE)
                .digitalWallet(DigitalWallet.APPLEPAY)
                .isDefault(true)
                .balance(new BigDecimal(10))
                .build();

        cardProduct = CardProduct
                .builder()
                .cardName("TEST")
                .paymentSystem(PaymentSystem.VISA)
                .coBrand(CoBrand.AEROFLOT)
                .isVirtual(Boolean.FALSE)
                .premiumStatus(PremiumStatus.CLASSIC)
                .servicePrice(BigDecimal.valueOf(0))
                .productPrice(BigDecimal.valueOf(0))
                .currencyCode(CurrencyCode.RUB)
                .isActive(Boolean.TRUE)
                .cardDuration(5)
                .build();

        account = accountRepository.save(account);
        product = productRepository.save(product);
        cardProduct = cardProductRepository.save(cardProduct);
        card.setAccount(account);
        card.setCardProduct(cardProduct);
        card = cardRepository.save(card);

        agreement = Agreement.builder()
                .number("V34y6sAMJLgXxU4XS2hq")
                .interestRate(new BigDecimal(1))
                .startDate(LocalDateTime.of(2020, 1, 15, 12, 0))
                .endDate(LocalDateTime.of(2029, 1, 15, 12, 0))
                .isActive(Boolean.TRUE)
                .autoRenewal(Boolean.FALSE)
                .product(product)
                .initialAmount(BigDecimal.valueOf(50))
                .currentBalance(BigDecimal.valueOf(500.1234))
                .account(account)
                .build();

    }

    @Test
    @DisplayName("Send revocation deposit order, should save in db operation")
    void sendRevocationDeposit_shouldSaveOperation() {
        //ARRANGE
        agreement = agreementRepository.save(agreement);
        List<Operation> listOperationBeforeMethod = operationRepository.findAll();

        //ACT
        agreementService.earlyWithdrawalDeposit(CLIENT_ID, agreement.getId(), withdrawDepositDto);

        List<Operation> listOperationAfterMethod = operationRepository.findAll();

        //  VERIFY
        assertThat(listOperationBeforeMethod).isNotEqualTo(listOperationAfterMethod);
        assertThat(listOperationAfterMethod.get(0)).isNotNull();

    }

    @Test
    @DisplayName("Withdraw event, should modify agreement and create operation")
    void modifyAgreementAndCreateOperation_shouldUpdateAgreement_andCreateOperation() {
        //ARRANGE
        agreement = agreementRepository.save(agreement);
        consumerWithdrawEvent = ConsumerWithdrawEvent
                .builder()
                .agreementId(agreement.getId())
                .accountNumber(ACCOUNT_NUMBER)
                .sum(new BigDecimal("10.1234"))
                .completedAt("2022-12-03T10:15:30")
                .currencyCode(CurrencyCode.RUB)
                .isActive(Boolean.TRUE)
                .currentBalance(new BigDecimal("999.1234"))
                .build();
        //ACT
        assertDoesNotThrow(() -> agreementService.modifyAgreementAndCreateOperation(consumerWithdrawEvent));
    }

    @Test
    @DisplayName("Update auto-renewal of active agreement")
    void sendRenewalDeposit_shouldUpdateAgreement() {
        //ARRANGE
        agreement.setIsActive(Boolean.TRUE);
        agreement.setAutoRenewal(Boolean.FALSE);
        agreement = agreementRepository.save(agreement);
        //ACT
        agreementService.updateAutoRenewal(CLIENT_ID, agreement.getId(), autoRenewalDto);
        //VERIFY
        Agreement agr = agreementRepository
                .findByAccountClientIdAndId(CLIENT_ID, agreement.getId())
                .orElseThrow();
        verifyAutoRenewal(agr);
    }

    private void verifyAutoRenewal(Agreement actual) {
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.getAutoRenewal())
                    .isEqualTo(true);
        });
    }

}
