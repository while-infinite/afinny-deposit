package by.afinny.deposit.integration.service;

import by.afinny.deposit.dto.*;
import by.afinny.deposit.entity.Account;
import by.afinny.deposit.entity.Card;
import by.afinny.deposit.entity.CardProduct;
import by.afinny.deposit.entity.constant.*;
import by.afinny.deposit.exception.CardStatusesAreEqualsException;
import by.afinny.deposit.exception.EntityNotFoundException;
import by.afinny.deposit.integration.config.annotation.TestWithPostgresContainer;
import by.afinny.deposit.repository.AccountRepository;
import by.afinny.deposit.repository.CardProductRepository;
import by.afinny.deposit.repository.CardRepository;
import by.afinny.deposit.service.CardService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.jdbc.Sql;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;


@SuppressWarnings("FieldCanBeLocal")
@TestWithPostgresContainer
@Sql(scripts = "/truncate.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("CardServiceIT")
public class CardServiceImplIT {

    private final String CARD_NUMBER = "123-122-123-124 ";
    private final String CARD_PIN_CODE = "12345";
    private final UUID CLIENT_ID = UUID.randomUUID();
    private Account account;
    private Card card;
    private CardProduct cardProduct;
    private CardStatusDto cardStatusDto;
    private CardDebitLimitDto cardDebitLimitDto;
    private NewPinCodeDebitCardDto newPinCodeDebitCardDto;
    @SpyBean
    private CardService cardService;
    @SpyBean
    private CardRepository cardRepository;
    @SpyBean
    private AccountRepository accountRepository;
    @SpyBean
    private CardProductRepository cardProductRepository;

    @BeforeAll
    void setUp() {
        cardStatusDto = CardStatusDto
                .builder()
                .cardStatus(CardStatus.CLOSED)
                .cardNumber("12345")
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
                .balance(BigDecimal.valueOf(100))
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
    }

    @Test
    @DisplayName("Change card status, should modify card")
    void changeCardStatus_shouldModifyCard() {
        //ARRANGE
        card.setStatus(CardStatus.ACTIVE);
        card = cardRepository.save(card);
        //ACT
        cardService.changeCardStatus(CLIENT_ID, cardStatusDto);
        //VERIFY
        Card foundCard = cardRepository.findByAccountClientIdAndId(CLIENT_ID, card.getId())
                .orElseThrow(() -> new EntityNotFoundException("Card with card number " + card.getId() + " for client id " + CLIENT_ID + " wasn't found"));
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(foundCard.getStatus())
                    .isEqualTo(cardStatusDto.getCardStatus());
        });
    }


    @Test
    @DisplayName("Change card status, should throw CardStatusesAreEqualsException if statuses are the same")
    void changeCardStatus_shouldThrowCardStatusesAreEqualsException() {
        //ARRANGE
        card.setStatus(cardStatusDto.getCardStatus());
        card.setCardNumber(cardStatusDto.getCardNumber());
        card = cardRepository.save(card);
        //ACT
        Throwable thrown = catchThrowable(() -> {
            cardService.changeCardStatus(CLIENT_ID, cardStatusDto);
        });
        //VERIFY
        assertThat(thrown)
                .isInstanceOf(CardStatusesAreEqualsException.class);
    }

    @Test
    @DisplayName("Change card status, should modify card")
    void modifyCardStatus_shouldModifyCard() {
        //ARRANGE
        card.setStatus(CardStatus.ACTIVE);
        card = cardRepository.save(card);
        //ACT
        cardService.modifyCardStatus(card.getId(), CardStatus.BLOCKED);
        //VERIFY
        Card foundCard = cardRepository.findByAccountClientIdAndId(CLIENT_ID, card.getId())
                .orElseThrow(() -> new EntityNotFoundException("Card with card number " + card.getId() + " for client id " + CLIENT_ID + " wasn't found"));
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(foundCard.getStatus())
                    .isEqualTo(CardStatus.BLOCKED);
        });
    }

    @Test
    @DisplayName("Change card status, should throw CardStatusesAreEqualsException if statuses are the same")
    void modifyCardStatus_shouldThrowCardStatusesAreEqualsException() {
        //ARRANGE
        card.setStatus(CardStatus.BLOCKED);
        card = cardRepository.save(card);
        //ACT
        Throwable thrown = catchThrowable(() -> {
            cardService.modifyCardStatus(card.getId(), CardStatus.BLOCKED);
        });
        //VERIFY
        assertThat(thrown)
                .isInstanceOf(CardStatusesAreEqualsException.class);
    }

    @Test
    @DisplayName("Change card limit, should modify card")
    void changeDebitCardLimit_shouldModifyCard() {
        //ARRANGE
        card.setTransactionLimit(new BigDecimal("100.1234"));
        card = cardRepository.save(card);
        //ACT
        cardService.changeDebitCardLimit(CLIENT_ID, cardDebitLimitDto);
        //VERIFY
        Card foundCard = cardRepository.findByAccountClientIdAndId(CLIENT_ID, card.getId())
                .orElseThrow(() -> new EntityNotFoundException("Card with card number " + card.getId() + " for client id " + CLIENT_ID + " wasn't found"));
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(foundCard.getTransactionLimit())
                    .isEqualTo(cardDebitLimitDto.getTransactionLimit());
        });
    }

    @Test
    @DisplayName("Should delete card from test container by card id")
    void deleteDebitCard_shouldNotThrow() {
        //ARRANGE
        card = cardRepository.save(card);
        //ACT
        assertDoesNotThrow(() -> cardService.deleteDebitCard(CLIENT_ID, card.getId()));
        //VERIFY
        Assertions.assertThat(
                cardRepository.findByAccountClientIdAndId(CLIENT_ID, card.getId()))
                .isEmpty();
    }

    @Test
    @DisplayName("Should return card from test container by card id")
    void getCardNumberByCardId_shouldReturnCard() {
        //ARRANGE
        card = cardRepository.save(card);
        //ACT
        cardService.getCardNumberByCardId(card.getId());
        //VERIFY
        Assertions.assertThat(
                cardRepository.findByAccountClientIdAndId(CLIENT_ID, card.getId()))
                .isPresent();
    }

    @Test
    @DisplayName("Change card pin code, should not throw")
    void changePinCodeDebitCard_shouldReturnOkResponse() {
        //ARRANGE
        card = cardRepository.save(card);
        //ACT & VERIFY
        assertDoesNotThrow(() -> cardService.changePinCodeDebitCard(CLIENT_ID, newPinCodeDebitCardDto));
    }

    @Test
    @DisplayName("Should return card from test container by card id, in case card not in close status")
    void getCardInfoByCardId_shouldReturnCard() throws Exception {
        //ARRANGE
        card.setStatus(CardStatus.ACTIVE);
        card = cardRepository.save(card);
        //ACT
        CardInfoDto cardInfoDto = cardService.getCardInfo(CLIENT_ID, card.getId());
        //VERIFY
        Assertions.assertThat(cardInfoDto).isNotNull();
    }
}
