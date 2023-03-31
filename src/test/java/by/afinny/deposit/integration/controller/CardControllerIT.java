package by.afinny.deposit.integration.controller;

import by.afinny.deposit.dto.*;
import by.afinny.deposit.entity.Account;
import by.afinny.deposit.entity.Card;
import by.afinny.deposit.entity.CardProduct;
import by.afinny.deposit.entity.constant.*;
import by.afinny.deposit.exception.EntityNotFoundException;
import by.afinny.deposit.integration.config.annotation.TestWithPostgresContainer;
import by.afinny.deposit.mapper.AccountMapper;
import by.afinny.deposit.mapper.CardMapper;
import by.afinny.deposit.repository.AccountRepository;
import by.afinny.deposit.repository.CardProductRepository;
import by.afinny.deposit.repository.CardRepository;
import by.afinny.deposit.utils.MappingUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import static by.afinny.deposit.entity.constant.CardStatus.BLOCKED;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("FieldCanBeLocal")
@TestWithPostgresContainer
@Sql(scripts = "/truncate.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("CardControllerIT")
public class CardControllerIT {

    private final String CARD_NUMBER = "123-122-123-124 ";
    private final String CARD_PIN_CODE = "12345";
    private final UUID CLIENT_ID = UUID.randomUUID();
    private Account account;
    private Card card;
    private CardProduct cardProduct;
    private List<Card> cards;
    private List<Account> accounts;
    private CardStatusDto cardStatusDto;
    private CardDebitLimitDto cardDebitLimitDto;
    private NewPinCodeDebitCardDto newPinCodeDebitCardDto;
    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private CardProductRepository cardProductRepository;
    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    private CardMapper cardMapper;
    @Autowired
    private MappingUtils mappingUtils;
    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    void setUp() {
        cardStatusDto = CardStatusDto
                .builder()
                .cardStatus(CardStatus.CLOSED)
                .build();

        cardDebitLimitDto = CardDebitLimitDto
                .builder()
                .cardNumber(CARD_NUMBER)
                .transactionLimit(new BigDecimal("90000.1234"))
                .build();

        newPinCodeDebitCardDto = NewPinCodeDebitCardDto
                .builder()
                .cardNumber(CARD_NUMBER)
                .newPin(CARD_PIN_CODE)
                .build();
    }

    @BeforeEach
    void save() {
        account = Account
                .builder()
                .accountNumber("1")
                .clientId(CLIENT_ID)
                .currentBalance(new BigDecimal("100.1234"))
                .openDate(LocalDate.of(2020, 1, 15))
                .closeDate(LocalDate.of(2030, 1, 15))
                .isActive(true)
                .salaryProject("salaryProject")
                .currencyCode(CurrencyCode.RUB)
                .blockedSum(new BigDecimal(10))
                .build();

        card = Card.builder()
                .cardNumber(CARD_NUMBER)
                .transactionLimit(new BigDecimal("100.1234"))
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
        cardProduct = cardProductRepository.save(cardProduct);
        card.setAccount(account);
        card.setCardProduct(cardProduct);
        card = cardRepository.save(card);
        cards = List.of(card);
        account.setCards(cards);
        accounts = List.of(account);
    }

    @Test
    @DisplayName("Should return cards list of active accounts from test container")
    void getActiveCards_shouldReturnCards() throws Exception {
        //ACT
        MvcResult result = mockMvc.perform(get("/auth/deposit-cards/")
                .param("clientId", CLIENT_ID.toString()))
                .andExpect(status().isOk())
                .andReturn();
        List<AccountWithCardInfoDto> accountsWithCardsDto = mappingUtils.getObjectListFromJson(result.getResponse().getContentAsString(), AccountWithCardInfoDto.class);
        List<AccountWithCardInfoDto> accountsWithCardsDtoDB = getActiveDepositsFromAccounts(accounts);
        //VERIFY
        verifyActiveProducts(accountsWithCardsDto, accountsWithCardsDtoDB);
    }

    @Test
    @DisplayName("Should return card from test container by account id and card id")
    void getCardByCardId_shouldReturnViewCard() throws Exception {
        //ACT
        MvcResult result = mockMvc.perform(get("/auth/deposit-cards/" + card.getId().toString())
                .param("clientId", CLIENT_ID.toString()))
                .andExpect(status().isOk())
                .andReturn();
        ViewCardDto viewCardDto = mappingUtils.getObjectFromJson(result.getResponse().getContentAsString(), ViewCardDto.class);
        ViewCardDto viewCardDtoDB = cardMapper.toViewCardDto(cardRepository
                .findByAccountClientIdAndId(CLIENT_ID, card.getId())
                .orElseThrow());
        //VERIFY
        verifyViewCardDto(viewCardDto, viewCardDtoDB);
    }

    @Test
    @DisplayName("Should return card from test container by card id")
    void getCardNumberByCardId_shouldReturnCard() throws Exception {
        //ACT
        MvcResult result = mockMvc.perform(get("/auth/deposit-cards/" + card.getId().toString() + "/information"))
                .andExpect(status().isOk())
                .andReturn();
        CardNumberDto cardNumberDto = mappingUtils.getObjectFromJson(result.getResponse().getContentAsString(), CardNumberDto.class);
        CardNumberDto cardNumberDtoDB = cardMapper.toCardNumberDto(card.getCardNumber());
        //VERIFY
        verifyCardNumberDto(cardNumberDto, cardNumberDtoDB);
    }

    @Test
    @DisplayName("Should return card from test container by card id, in case card not in close status")
    void getCardInfoByCardId_shouldReturnCard() throws Exception {
        //ACT
        MvcResult result = mockMvc.perform(get("/auth/deposit-cards/" + card.getId().toString() + "/info")
                .param("clientId", CLIENT_ID.toString()))
                .andExpect(status().isOk())
                .andReturn();
        CardInfoDto cardInfoDto = mappingUtils.getObjectFromJson(result.getResponse().getContentAsString(), CardInfoDto.class);
        CardInfoDto cardInfoDtoDB = cardMapper.toCardInfoDto(card);
        //VERIFY
        verifyCardInfoDto(cardInfoDto, cardInfoDtoDB);
    }

    @Test
    @DisplayName("Change card status, should return ok status")
    void changeCardStatus_shouldReturnOkStatus() throws Exception {
        //ACT
        mockMvc.perform(
                patch("/auth/deposit-cards/" + card.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("clientId", CLIENT_ID.toString())
                        .content(mappingUtils.asJsonString(cardStatusDto)))
                .andExpect(status().isOk());
        //VERIFY
        Card foundCard = cardRepository.findByAccountClientIdAndId(CLIENT_ID, card.getId())
                .orElseThrow(() -> new EntityNotFoundException("Card with card number " + card.getId() + " for client id " + CLIENT_ID + " wasn't found"));
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(foundCard.getStatus())
                    .isEqualTo(cardStatusDto.getCardStatus());
        });
    }

    @Test
    @DisplayName("Change card limit, should return  ok status")
    void changeDebitCardLimit_shouldReturnOkStatus() throws Exception {
        //ACT
        mockMvc.perform(
                patch("/auth/deposit-cards/limit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("clientId", CLIENT_ID.toString())
                        .content(mappingUtils.asJsonString(cardDebitLimitDto)))
                .andExpect(status().isOk());
        //VERIFY
        Card foundCard = cardRepository.findByAccountClientIdAndId(CLIENT_ID, card.getId())
                .orElseThrow(() -> new EntityNotFoundException("Card with card number " + card.getId() + " for client id " + CLIENT_ID + " wasn't found"));
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(foundCard.getTransactionLimit())
                    .isEqualTo(cardDebitLimitDto.getTransactionLimit());
        });
    }

    @Test
    @DisplayName("Change card pin code, should return ok status")
    void changePinCodeDebitCard_shouldReturnOkResponse() throws Exception {
        //ACT
        ResultActions result = mockMvc.perform(
                post("/auth/deposit-cards/code/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("clientId", CLIENT_ID.toString())
                        .content(mappingUtils.asJsonString(newPinCodeDebitCardDto)));
        //VERIFY
        result.andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should delete card from test container by card id")
    void deleteDebitCard_shouldReturnOkResponse() throws Exception {
        //ACT
        mockMvc.perform(
                delete("/auth/deposit-cards/" + card.getId().toString())
                        .param("clientId", CLIENT_ID.toString()))
                .andExpect(status().isNoContent())
                .andReturn();
        //VERIFY
        Assertions.assertThat(
                cardRepository.findByAccountClientIdAndId(CLIENT_ID, card.getId()))
                .isEmpty();
    }

    private List<AccountWithCardInfoDto> getActiveDepositsFromAccounts(List<Account> accounts) {
        List<Card> cards = accounts
                .stream()
                .flatMap(a -> a.getCards().stream())
                .filter(c -> c.getStatus() != BLOCKED)
                .collect(Collectors.toList());
        return accountMapper.toAccountsWithCardsDto(cards);
    }

    private void verifyViewCardDto(ViewCardDto result, ViewCardDto actual) {
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(result.getCardId())
                    .isEqualTo(actual.getCardId());
            softAssertions.assertThat(result.getHolderName())
                    .isEqualTo(actual.getHolderName());
            softAssertions.assertThat(result.getStatus())
                    .isEqualTo(actual.getStatus());
            softAssertions.assertThat(result.getCardNumber())
                    .isEqualTo(actual.getCardNumber());
            softAssertions.assertThat(result.getExpirationDate())
                    .isEqualTo(actual.getExpirationDate());
            softAssertions.assertThat(result.getCardName())
                    .isEqualTo(actual.getCardName());
            softAssertions.assertThat(result.getPaymentSystem())
                    .isEqualTo(actual.getPaymentSystem());
            softAssertions.assertThat(result.getCurrencyCode())
                    .isEqualTo(actual.getCurrencyCode());
            softAssertions.assertThat(result.getCardBalance())
                    .isEqualTo(actual.getCardBalance());
            softAssertions.assertThat(result.getAccountId())
                    .isEqualTo(actual.getAccountId());
            softAssertions.assertThat(result.getAccountNumber())
                    .isEqualTo(actual.getAccountNumber());
        });
    }

    private void verifyCardNumberDto(CardNumberDto result, CardNumberDto actual) {
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(result.getCardNumber())
                    .isEqualTo(actual.getCardNumber());
        });
    }

    private void verifyCardInfoDto(CardInfoDto result, CardInfoDto actual) {
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(result.getHolderName())
                    .isEqualTo(actual.getHolderName());
            softAssertions.assertThat(result.getStatus())
                    .isEqualTo(actual.getStatus());
            softAssertions.assertThat(result.getTransactionLimit())
                    .isEqualTo(actual.getTransactionLimit());
        });
    }

    private void verifyActiveProducts(List<AccountWithCardInfoDto> result, List<AccountWithCardInfoDto> actual) {
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(actual);
    }
}
