package by.afinny.deposit.integration.controller;

import by.afinny.deposit.dto.ActiveDepositDto;
import by.afinny.deposit.dto.AutoRenewalDto;
import by.afinny.deposit.dto.WithdrawDepositDto;
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
import by.afinny.deposit.exception.EntityNotFoundException;
import by.afinny.deposit.integration.config.annotation.TestWithPostgresContainer;
import by.afinny.deposit.mapper.DepositMapper;
import by.afinny.deposit.repository.AccountRepository;
import by.afinny.deposit.repository.AgreementRepository;
import by.afinny.deposit.repository.CardProductRepository;
import by.afinny.deposit.repository.CardRepository;
import by.afinny.deposit.repository.OperationRepository;
import by.afinny.deposit.repository.ProductRepository;
import by.afinny.deposit.utils.MappingUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("FieldCanBeLocal")
@TestWithPostgresContainer
@Sql(scripts = "/truncate.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("DepositControllerIT")
public class DepositControllerIT {
    @SpyBean
    private OperationRepository operationRepository;
    private Agreement agreement;
    private List<Agreement> agreements;
    private Product product;
    private Account account;
    private Card card;
    private List<Card> cards;
    private CardProduct cardProduct;
    private final UUID CLIENT_ID = UUID.randomUUID();
    private final String CARD_NUMBER = "123-122-123-124 ";
    private final String ACCOUNT_NUMBER = "1";
    private WithdrawDepositDto withdrawDepositDto;
    private AutoRenewalDto autoRenewalDto;
    @Autowired
    private AgreementRepository agreementRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private CardProductRepository cardProductRepository;
    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private MappingUtils mappingUtils;
    @Autowired
    private DepositMapper depositMapper;

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
    void getDeposits_shouldReturnDeposits() throws Exception {
        //ARRANGE
        agreement.setIsActive(Boolean.TRUE);
        agreement = agreementRepository.save(agreement);
        agreements = List.of(agreement);
        //ACT
        MvcResult result = mockMvc.perform(get("/auth/deposits/")
                        .param("clientId", CLIENT_ID.toString()))
                .andExpect(status().isOk())
                .andReturn();
        List<ActiveDepositDto> depositDto = mappingUtils.getObjectListFromJson(result.getResponse().getContentAsString(), ActiveDepositDto.class);
        List<ActiveDepositDto> depositDtoDB = depositMapper.toActiveDepositsDto(agreements);
        //VERIFY
        verifyDeposits(depositDto, depositDtoDB);
    }

    @Test
    @DisplayName("Should return empty deposits list in case no active deposits in db")
    void getDeposits_shouldReturnEmptyDeposits() throws Exception {
        //ARRANGE
        agreement.setIsActive(Boolean.FALSE);
        agreement = agreementRepository.save(agreement);
        agreements = List.of(agreement);
        //ACT
        MvcResult result = mockMvc.perform(get("/auth/deposits/")
                        .param("clientId", CLIENT_ID.toString()))
                .andExpect(status().isOk())
                .andReturn();
        List<ActiveDepositDto> depositDto = mappingUtils.getObjectListFromJson(result.getResponse().getContentAsString(), ActiveDepositDto.class);
        //VERIFY
        assertThat(depositDto.isEmpty())
                .isTrue();
    }

    @Test
    @DisplayName("Should return empty deposits list in case of wrong client id")
    void getDeposits_shouldReturnNoDeposits() throws Exception {
        //ARRANGE
        agreement.setIsActive(Boolean.TRUE);
        agreement = agreementRepository.save(agreement);
        agreements = List.of(agreement);
        //ACT
        MvcResult result = mockMvc.perform(get("/auth/deposits/")
                        .param("clientId", UUID.randomUUID().toString()))
                .andExpect(status().isOk())
                .andReturn();
        List<ActiveDepositDto> depositDto = mappingUtils.getObjectListFromJson(result.getResponse().getContentAsString(), ActiveDepositDto.class);
        //VERIFY
        assertThat(depositDto.isEmpty())
                .isTrue();
    }

    @Test
    @DisplayName("Should return deposit from test container by deposit id and card id")
    void getDeposit_shouldReturnDeposit() throws Exception {
        //ARRANGE
        agreement.setIsActive(Boolean.TRUE);
        agreement = agreementRepository.save(agreement);
        agreements = List.of(agreement);
        //ACT
        MvcResult result = mockMvc.perform(get("/auth/deposits/" + agreement.getId())
                        .param("clientId", CLIENT_ID.toString())
                        .param("cardId", card.getId().toString()))
                .andExpect(status().isOk())
                .andReturn();
        ActiveDepositDto depositDto = mappingUtils.getObjectFromJson(result.getResponse().getContentAsString(), ActiveDepositDto.class);
        ActiveDepositDto depositDtoDB = depositMapper.toActiveDepositDto(agreement);
        //VERIFY
        verifyDepositDto(depositDto, depositDtoDB);
    }

    @ParameterizedTest
    @MethodSource("generateData")
    @DisplayName("If agreement id or card id is incorrect then return bad request status")
    void getDeposit_ifNotSuccess_thenStatusIsBadRequest(UUID agreementID, UUID cardID) throws Exception {
        //ARRANGE
        agreement.setIsActive(Boolean.TRUE);
        agreement = agreementRepository.save(agreement);
        agreements = List.of(agreement);
        //ACT
        ResultActions resultActions = mockMvc.perform(get("/auth/deposits/" + agreementID)
                .param("clientId", CLIENT_ID.toString())
                .param("cardId", cardID.toString()));
        //VERIFY
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(result -> Assertions.assertThat(result.getResolvedException()).isInstanceOf(EntityNotFoundException.class));
    }

    @Test
    @DisplayName("Send revocation deposit order, should save in db operation ")
    void sendRevocationDeposit_shouldSaveOperation() throws Exception {
        //ARRANGE
        agreement.setIsActive(Boolean.TRUE);
        agreement = agreementRepository.save(agreement);
        agreements = List.of(agreement);

        List<Operation> listOperationBeforeMethod = operationRepository.findAll();

        //ACT
        ResultActions resultActions = mockMvc.perform(
                patch("/auth/deposits/" + agreement.getId() + "/revocation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("clientId", CLIENT_ID.toString())
                        .param("cardId", card.getId().toString())
                        .content(mappingUtils.asJsonString(withdrawDepositDto)));
        //VERIFY
        List<Operation> listOperationAfterMethod = operationRepository.findAll();

        assertThat(listOperationBeforeMethod).isNotEqualTo(listOperationAfterMethod);
        assertThat(listOperationAfterMethod.get(0)).isNotNull();

    }

    @Test
    @DisplayName("Send auto-renewal order, should return ok status")
    void sendRenewalDeposit_shouldReturnOkResponse() throws Exception {
        //ARRANGE
        agreement.setIsActive(Boolean.TRUE);
        agreement = agreementRepository.save(agreement);
        agreements = List.of(agreement);
        //ACT
        mockMvc.perform(
                        patch("/auth/deposits/" + agreement.getId() + "/auto-renewal")
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("clientId", CLIENT_ID.toString())
                                .content(mappingUtils.asJsonString(autoRenewalDto)))
                .andExpect(status().isOk());
        //VERIFY
        Agreement agr = agreementRepository
                .findByAccountClientIdAndId(CLIENT_ID, agreement.getId())
                .orElseThrow();
        verifyAutoRenewal(agr);
    }

    @Test
    @DisplayName("Send auto-renewal order, if agreement not active should return bad request status")
    void sendRenewalDeposit_shouldReturnBadRequestResponse() throws Exception {
        //ARRANGE
        agreement.setIsActive(Boolean.FALSE);
        agreement = agreementRepository.save(agreement);
        agreements = List.of(agreement);
        //ACT
        ResultActions resultActions = mockMvc.perform(
                patch("/auth/deposits/" + agreement.getId() + "/auto-renewal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("clientId", CLIENT_ID.toString())
                        .content(mappingUtils.asJsonString(autoRenewalDto)));
        //VERIFY
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(result -> Assertions.assertThat(result.getResolvedException()).isInstanceOf(EntityNotFoundException.class));
    }

    private Stream<Arguments> generateData() {
        return Stream.of(
                Arguments.of(agreement.getId(), UUID.randomUUID()),
                Arguments.of(UUID.randomUUID(), card.getId()),
                Arguments.of(UUID.randomUUID(), UUID.randomUUID()));
    }

    private void verifyDeposits(List<ActiveDepositDto> result, List<ActiveDepositDto> actual) {
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(actual);
    }

    private void verifyDepositDto(ActiveDepositDto expected, ActiveDepositDto actual) {
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

    private void verifyAutoRenewal(Agreement actual) {
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.getAutoRenewal())
                    .isEqualTo(true);
        });
    }
}
