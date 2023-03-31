package by.afinny.deposit.integration.controller;

import by.afinny.deposit.dto.RequestNewDepositDto;
import by.afinny.deposit.entity.*;
import by.afinny.deposit.entity.constant.*;
import by.afinny.deposit.exception.EntityNotFoundException;
import by.afinny.deposit.integration.config.annotation.TestWithPostgresContainer;
import by.afinny.deposit.repository.*;
import by.afinny.deposit.utils.MappingUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("FieldCanBeLocal")
@TestWithPostgresContainer
@Sql(scripts = "/truncate.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("DepositOrderControllerIT")
public class DepositOrderControllerIT {

    private Card card;
    private Product product;
    private CardProduct cardProduct;
    private Account account;
    private RequestNewDepositDto requestNewDepositDto;
    private final UUID CLIENT_ID = UUID.randomUUID();
    private final String CARD_NUMBER = "123-122-123-124";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CardProductRepository cardProductRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private MappingUtils mappingUtils;

    @BeforeAll
    void setUp() {
        requestNewDepositDto = RequestNewDepositDto.builder()
                .initialAmount(new BigDecimal(10))
                .cardNumber(CARD_NUMBER)
                .autoRenewal(true)
                .interestRate(new BigDecimal(10))
                .durationMonth(10)
                .build();

        card = Card.builder()
                .id(UUID.randomUUID())
                .cardNumber(CARD_NUMBER)
                .transactionLimit(new BigDecimal("100"))
                .expirationDate(LocalDate.now().plusDays(1))
                .holderName("Peter Parker")
                .status(CardStatus.ACTIVE)
                .digitalWallet(DigitalWallet.APPLEPAY)
                .isDefault(true)
                .account(Account.builder()
                .clientId(CLIENT_ID).build())
                .build();

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

        account = Account
                .builder()
                .accountNumber("1")
                .clientId(CLIENT_ID)
                .currentBalance(new BigDecimal(100))
                .openDate(LocalDate.of(2020, 1, 15))
                .closeDate(LocalDate.of(2030, 1, 15))
                .isActive(true)
                .salaryProject("salaryProject")
                .currencyCode(CurrencyCode.RUB)
                .blockedSum(new BigDecimal(10))
                .build();
    }

    @BeforeEach
    void save() {
        account = accountRepository.save(account);
        product = productRepository.save(product);
        cardProduct = cardProductRepository.save(cardProduct);
        card.setAccount(account);
        card.setCardProduct(cardProduct);
        cardRepository.save(card);
        requestNewDepositDto.setProductId(product.getId());
    }

    @Test
    @DisplayName("Send new deposit order, should return ok status")
    void sendNewDeposit_shouldReturnOkResponse() throws Exception {
        //ARRANGE
        requestNewDepositDto.setCardNumber(CARD_NUMBER);
        //ACT & VERIFY
        mockMvc.perform(
                post("/auth/deposit-orders/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("clientId", CLIENT_ID.toString())
                        .content(mappingUtils.asJsonString(requestNewDepositDto)))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @MethodSource("generateData")
    @DisplayName("If client id or card id is incorrect then return unauthorized status")
    void sendNewDeposit_ifClientId_or_CardId_isWrong_thenStatusIsBadRequest(UUID clientId, UUID cardId) throws Exception {
        //ARRANGE
        requestNewDepositDto.setCardNumber(cardId.toString());
        //ACT
        ResultActions resultActions = mockMvc.perform(
                post("/auth/deposit-orders/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("clientId", clientId.toString())
                        .content(mappingUtils.asJsonString(requestNewDepositDto)));
        //VERIFY
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertThat(result.getResolvedException()).isInstanceOf(EntityNotFoundException.class));
    }

    private Stream<Arguments> generateData() {
        return Stream.of(
                Arguments.of(UUID.randomUUID(), card.getId()),
                Arguments.of(CLIENT_ID, UUID.randomUUID()),
                Arguments.of(UUID.randomUUID(), UUID.randomUUID()));
    }
}
