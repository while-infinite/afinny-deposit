package by.afinny.deposit.integration.service;

import by.afinny.deposit.dto.RequestNewCardDto;
import by.afinny.deposit.dto.kafka.ConsumerNewCardEvent;
import by.afinny.deposit.entity.Account;
import by.afinny.deposit.entity.Card;
import by.afinny.deposit.entity.CardProduct;
import by.afinny.deposit.entity.Product;
import by.afinny.deposit.entity.constant.*;
import by.afinny.deposit.exception.EntityNotFoundException;
import by.afinny.deposit.integration.config.annotation.TestWithPostgresContainer;
import by.afinny.deposit.repository.AccountRepository;
import by.afinny.deposit.repository.CardProductRepository;
import by.afinny.deposit.repository.CardRepository;
import by.afinny.deposit.repository.ProductRepository;
import by.afinny.deposit.service.OrderService;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.jdbc.Sql;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@TestWithPostgresContainer
@Sql(scripts = "/truncate.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("OrderServiceIT")
public class OrderServiceImplIT {

    private final String CARD_NUMBER = "123-122-123-124 ";
    private final String ACCOUNT_NUMBER = "1";
    private final String HOLDER_NAME = "Peter Parker";
    private RequestNewCardDto requestNewCardDto;
    private ConsumerNewCardEvent consumerNewCardEvent;
    private Account account;
    private Product product;
    private CardProduct cardProduct;
    private final UUID CLIENT_ID = UUID.randomUUID();
    @SpyBean
    private OrderService orderService;
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
    }

    @Test
    @DisplayName("Order new card should not throw exception")
    void orderNewCard_ifProductIdCorrect_thenDoNotThrow()  {
        //ARRANGE
        product = productRepository.save(product);
        requestNewCardDto = RequestNewCardDto
                .builder()
                .productId(product.getId())
                .build();
        //ACT & VERIFY
        assertDoesNotThrow(() -> orderService.orderNewCard(CLIENT_ID, requestNewCardDto));
    }

    @Test
    @DisplayName("Order new card should throw exception in case of wrong product id")
    void orderNewCard_ifProductIdWrong_throwEntityNotFoundException()  {
        //ARRANGE
        int wrongID = -44;
        product = productRepository.save(product);
        requestNewCardDto = RequestNewCardDto
                .builder()
                .productId(wrongID)
                .build();
        //ACT
        Throwable thrown = catchThrowable(() -> {
            orderService.orderNewCard(CLIENT_ID, requestNewCardDto);
        });
        //VERIFY
        assertThat(thrown)
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("product with id " + wrongID + " wasn't found");
    }

    @Test
    @DisplayName("Order new card should not throw exception")
    void createNewCard_ifProductIdCorrect_thenDoNotThrow()  {
        //ARRANGE
        account = accountRepository.save(account);
        cardProduct = cardProductRepository.save(cardProduct);
        consumerNewCardEvent = ConsumerNewCardEvent
                .builder()
                .accountNumber(ACCOUNT_NUMBER)
                .holderName(HOLDER_NAME)
                .cardNumber(CARD_NUMBER)
                .cardProductId(cardProduct.getId())
                .digitalWallet(DigitalWallet.APPLEPAY)
                .expirationDate(LocalDate.of(2030, 1, 15))
                .status(CardStatus.ACTIVE)
                .transactionLimit(new BigDecimal("1000.1234"))
                .build();
        //ACT
        assertDoesNotThrow(() -> orderService.createNewCard(consumerNewCardEvent));
        //VERIFY
        assertThat(cardRepository
                .findByAccountClientIdAndCardNumber(CLIENT_ID, CARD_NUMBER))
                .isPresent();
    }
}
