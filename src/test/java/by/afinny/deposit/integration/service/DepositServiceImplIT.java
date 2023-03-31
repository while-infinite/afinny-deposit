package by.afinny.deposit.integration.service;

import by.afinny.deposit.dto.*;
import by.afinny.deposit.entity.*;
import by.afinny.deposit.entity.constant.*;
import by.afinny.deposit.exception.EntityNotFoundException;
import by.afinny.deposit.integration.config.annotation.TestWithPostgresContainer;
import by.afinny.deposit.mapper.DepositMapper;
import by.afinny.deposit.repository.*;
import by.afinny.deposit.service.DepositService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.jdbc.Sql;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SuppressWarnings("FieldCanBeLocal")
@TestWithPostgresContainer
@Sql(scripts = "/truncate.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("DepositServiceIT")
public class DepositServiceImplIT {

    private Agreement agreement;
    private Product product;
    private Account account;
    private Card card;
    private List<Card> cards;
    private CardProduct cardProduct;
    private final UUID CLIENT_ID = UUID.randomUUID();
    private final String CARD_NUMBER = "123-122-123-124 ";
    private final String ACCOUNT_NUMBER = "1";
    private RequestNewDepositDto requestNewDepositDto;
    @SpyBean
    private DepositService depositService;
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
    @SpyBean
    private DepositMapper depositMapper;

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
        cards = List.of(card);
        account.setCards(cards);

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
    @DisplayName("Should return active deposits list from test container")
    void getProducts_shouldReturnProducts() {
        //ARRANGE
        agreement.setIsActive(Boolean.TRUE);
        agreementRepository.save(agreement);
        //ACT
        List<ActiveDepositDto> depositDtoResult = depositService.getActiveDeposits(CLIENT_ID);
        List<ActiveDepositDto> depositDtoActual = depositMapper.toActiveDepositsDto(agreementRepository.findByAccountClientIdAndIsActiveTrue(CLIENT_ID));
        //VERIFY
        verifyDeposits(depositDtoResult, depositDtoActual);
    }

    @Test
    @DisplayName("Should return empty deposits list in case no active deposits")
    void getProducts_shouldReturnEmptyProducts() {
        //ARRANGE
        agreement.setIsActive(Boolean.FALSE);
        agreementRepository.save(agreement);
        //ACT
        List<ActiveDepositDto> depositDtoResult = depositService.getActiveDeposits(CLIENT_ID);
        //VERIFY
        assertThat(depositDtoResult.isEmpty())
                .isTrue();
    }

    @Test
    @DisplayName("Should return deposit by client id, agreement id and card id")
    void getCardByCardId_shouldReturnViewCard() {
        //ARRANGE
        agreement = agreementRepository.save(agreement);
        //ACT
        DepositDto depositDtoResult = depositService.getDeposit(CLIENT_ID, agreement.getId(), card.getId());
        DepositDto depositDtoActual = getDepositDto(CLIENT_ID, agreement.getId(), card.getId());
        //VERIFY
        verifyDepositDto(depositDtoResult, depositDtoActual);
    }

    @Test
    @DisplayName("Should save agreement")
    void saveDeposit_shouldSaveAgreement() {
        //ACT
        depositService.saveAgreement(agreement);
        //VERIFY
        verifyThatDepositSaved(CLIENT_ID, agreement.getId());
    }

    @Test
    @DisplayName("Should rollback if agreement number is null")
    void saveDeposit_ifAgreementNumber_isNull_thenRollbackTransaction() {
        //ARRANGE
        agreement.setNumber(null);
        //ACT
        Throwable thrown = catchThrowable(() -> {
            depositService.saveAgreement(agreement);
        });
        //VERIFY
        assertThat(thrown)
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("could not execute statement");

        verifyThatDepositNotSaved(CLIENT_ID, agreement.getId());
    }

    @Test
    @DisplayName("Should not throw exception if verify request check passed successfully")
    void createNewDeposit_ifVerifyRequest_shouldNotThrow() {
        //ARRANGE
        requestNewDepositDto = RequestNewDepositDto
                .builder()
                .cardNumber(CARD_NUMBER)
                .productId(product.getId())
                .autoRenewal(Boolean.TRUE)
                .durationMonth(2)
                .initialAmount(new BigDecimal("10.1234"))
                .interestRate(new BigDecimal("1.1234"))
                .build();
        //ACT & VERIFY
        assertDoesNotThrow(() -> depositService.createNewDeposit(CLIENT_ID, requestNewDepositDto));
    }

    @Test
    @DisplayName("Should throw exception if verify request check do not passed")
    void createNewDeposit_ifVerifyRequestNotPassed_shouldThrowExeption() {
        //ARRANGE
        requestNewDepositDto = RequestNewDepositDto
                .builder()
                .cardNumber(CARD_NUMBER)
                .productId(65)
                .autoRenewal(Boolean.TRUE)
                .durationMonth(2)
                .initialAmount(new BigDecimal("10.1234"))
                .interestRate(new BigDecimal("1.1234"))
                .build();
        //ACT
        Throwable thrown = catchThrowable(() -> {
            depositService.createNewDeposit(CLIENT_ID, requestNewDepositDto);
        });
        //VERIFY
        assertThat(thrown)
                .isInstanceOf(EntityNotFoundException.class);
    }

    private DepositDto getDepositDto(UUID client_id, UUID agreement_id, UUID card_id) {
        Agreement agreement = agreementRepository.findByAccountClientIdAndId(client_id, agreement_id)
          .orElseThrow(() -> new EntityNotFoundException("agreement with id " + agreement_id + "for client id " + client_id + " not found"));
        Card card = cardRepository.findById(card_id)
           .orElseThrow(
                () -> new EntityNotFoundException("card with id " + card_id + " not found"));
        return depositMapper.toDepositDto(agreement, agreement.getProduct(), card);
    }

    private void verifyDeposits(List<ActiveDepositDto> result, List<ActiveDepositDto> actual) {
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(actual);
    }

    private void verifyDepositDto(DepositDto expected, DepositDto actual) {
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.getCardNumber())
                    .isEqualTo(expected.getCardNumber());
            softAssertions.assertThat(actual.getStartDate())
                    .isEqualTo(expected.getStartDate());
            softAssertions.assertThat(actual.getEndDate())
                    .isEqualTo(expected.getEndDate());
            softAssertions.assertThat(actual.getCurrentBalance())
                    .isEqualTo(expected.getCurrentBalance());
            softAssertions.assertThat(actual.getCurrencyCode())
                    .isEqualTo(expected.getCurrencyCode());
        });
    }

    private void verifyThatDepositSaved(UUID client_id, UUID agreement_id) {
        Assertions.assertThat(agreementRepository.findByAccountClientIdAndId(client_id, agreement_id))
                .isPresent();
    }

    private void verifyThatDepositNotSaved(UUID client_id, UUID agreement_id) {
        Assertions.assertThat(agreementRepository.findByAccountClientIdAndId(client_id, agreement_id))
                .isEmpty();
    }
}
