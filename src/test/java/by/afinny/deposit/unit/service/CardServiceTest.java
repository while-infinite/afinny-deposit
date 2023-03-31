package by.afinny.deposit.unit.service;

import by.afinny.deposit.dto.CardDebitLimitDto;
import by.afinny.deposit.dto.CardNumberDto;
import by.afinny.deposit.dto.CardStatusDto;
import by.afinny.deposit.dto.CreatePaymentDepositDto;
import by.afinny.deposit.dto.NewPinCodeDebitCardDto;
import by.afinny.deposit.entity.Card;
import by.afinny.deposit.entity.constant.CardStatus;
import by.afinny.deposit.entity.constant.DigitalWallet;
import by.afinny.deposit.exception.CardStatusesAreEqualsException;
import by.afinny.deposit.exception.EntityNotFoundException;
import by.afinny.deposit.mapper.CardMapper;
import by.afinny.deposit.repository.CardRepository;
import by.afinny.deposit.service.impl.CardServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@ActiveProfiles("test")
class CardServiceTest {

    @InjectMocks
    private CardServiceImpl cardService;
    @Mock
    private CardRepository cardRepository;

    @Spy
    private ApplicationEventPublisher publisher;

    @Spy
    private CardMapper cardMapper;
    private static final UUID CLIENT_ID = UUID.randomUUID();
    private final UUID CARD_ID = UUID.fromString("f0fca2da-e7d2-11ec-8fea-0242ac120002");
    private final CardStatus NEW_CARD_STATUS = CardStatus.ACTIVE;
    private final String CARD_NUMBER = "1111222233334444";
    private Card card;
    private Card card2;
    private Card cardNumber;
    private CardStatusDto cardStatusDto;
    private CardDebitLimitDto cardDebitLimitDto;
    private CardNumberDto cardNumberDto;
    private NewPinCodeDebitCardDto newPinCodeDebitCardDto;
    private CreatePaymentDepositDto createPaymentDepositDto;

    @BeforeEach
    void setUp() {
        card = Card.builder()
                .id(CARD_ID)
                .cardNumber("cardNumber")
                .transactionLimit(new BigDecimal(700))
                .status(CardStatus.BLOCKED)
                .expirationDate(LocalDate.of(2024, 1, 2))
                .holderName("holder_name")
                .digitalWallet(DigitalWallet.GOOGLEPAY)
                .isDefault(true)
                .build();

        cardStatusDto = CardStatusDto.builder()
                .cardStatus(NEW_CARD_STATUS)
                .cardNumber("12345")
                .build();

        cardNumber = Card.builder()
                .cardNumber("4544554")
                .build();

        cardNumberDto = CardNumberDto.builder()
                .cardNumber("12356545")
                .build();

        cardDebitLimitDto = CardDebitLimitDto.builder()
                .cardNumber(CARD_NUMBER)
                .transactionLimit(new BigDecimal(700))
                .build();

         newPinCodeDebitCardDto = NewPinCodeDebitCardDto.builder()
                 .cardNumber(CARD_NUMBER)
                 .build();

        createPaymentDepositDto = CreatePaymentDepositDto.builder()
                .remitterCardNumber("0000111100001111")
                .sum(BigDecimal.valueOf(300.0))
                .build();

        card2 = Card.builder()
                .id(UUID.randomUUID())
                .cardNumber("0000111100001111")
                .transactionLimit(BigDecimal.valueOf(100000.0))
                .status(CardStatus.ACTIVE)
                .expirationDate(LocalDate.of(2024, 1, 2))
                .holderName("holder_name")
                .isDefault(true)
                .balance(BigDecimal.valueOf(10000.0))
                .build();
    }

    @Test
    @DisplayName("If card was found then save")
    void changeCardStatus_shouldSave() {
        //ARRANGE
        when(cardRepository.findByAccountClientIdAndCardNumber(CLIENT_ID, cardStatusDto.getCardNumber())).thenReturn(Optional.of(card));

        //ACT
        cardService.changeCardStatus(CLIENT_ID, cardStatusDto);

        //VERIFY
        verify(cardRepository).save(card);
    }

    @Test
    @DisplayName("If cards statuses are equals")
    void changeCardStatus_ifStatusesEquals_thenThrow() {
        //ARRANGE
        card.setStatus(CardStatus.ACTIVE);
        when(cardRepository.findByAccountClientIdAndCardNumber(CLIENT_ID, cardStatusDto.getCardNumber())).thenReturn(Optional.of(card));

        //ACT
        ThrowingCallable createOrderMethodInvocation = () -> cardService.changeCardStatus(CLIENT_ID, cardStatusDto);

        //VERIFY
        assertThatThrownBy(createOrderMethodInvocation).isInstanceOf(CardStatusesAreEqualsException.class);
        verify(cardRepository, never()).save(card);
    }

    @Test
    @DisplayName("If card with incoming card number wasn't found")
    void changeCardStatus_ifCardNotFound_thenThrow() {
        //ARRANGE
        when(cardRepository.findByAccountClientIdAndCardNumber(CLIENT_ID, cardStatusDto.getCardNumber())).thenReturn(Optional.empty());

        //ACT
        ThrowingCallable createOrderMethodInvocation = () -> cardService.changeCardStatus(CLIENT_ID, cardStatusDto);

        //VERIFY
        assertThatThrownBy(createOrderMethodInvocation).isInstanceOf(EntityNotFoundException.class);
        verify(cardRepository, never()).save(card);
    }

    @Test
    @DisplayName("If card was found then save")
    void modifyCardStatus_shouldSave() {
        //ARRANGE
        when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));

        //ACT
        cardService.modifyCardStatus(CARD_ID, NEW_CARD_STATUS);

        //VERIFY
        verify(cardRepository).save(card);
    }

    @Test
    @DisplayName("If cards statuses are equals")
    void modifyCardStatus_ifStatusesEquals_thenThrow() {
        //ARRANGE
        when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));

        //ACT
        ThrowingCallable createOrderMethodInvocation = () -> cardService.modifyCardStatus(CARD_ID, CardStatus.BLOCKED);

        //VERIFY
        assertThatThrownBy(createOrderMethodInvocation).isInstanceOf(CardStatusesAreEqualsException.class);
        verify(cardRepository, never()).save(card);
    }

    @Test
    @DisplayName("If card with incoming card number wasn't found")
    void modifyCardStatus_ifCardNotFound_thenThrow() {
        //ARRANGE
        when(cardRepository.findById(CARD_ID)).thenReturn(Optional.empty());

        //ACT
        ThrowingCallable createOrderMethodInvocation = () -> cardService.modifyCardStatus(CARD_ID, NEW_CARD_STATUS);

        //VERIFY
        assertThatThrownBy(createOrderMethodInvocation).isInstanceOf(EntityNotFoundException.class);
        verify(cardRepository, never()).save(card);
    }

    @Test
    @DisplayName("If card was found then save")
    void changeDebitCardLimit_shouldSave() {
        //ARRANGE
        when(cardRepository.findByAccountClientIdAndCardNumber(CLIENT_ID, CARD_NUMBER)).thenReturn(Optional.of(card));

        //ACT
        cardService.changeDebitCardLimit(CLIENT_ID, cardDebitLimitDto);

        //VERIFY
        verifyDebitCardLimit(card);
        verify(cardRepository).save(card);
    }

    @Test
    @DisplayName("If card with incoming card number wasn't found")
    void changeDebitCardLimit_ifDebitCardLimitNotFound_thenThrow() {
        //ARRANGE
        when(cardRepository.findByAccountClientIdAndCardNumber(CLIENT_ID, CARD_NUMBER)).thenReturn(Optional.empty());

        //ACT
        ThrowingCallable createOrderMethodInvocation = () -> cardService.changeDebitCardLimit(CLIENT_ID, cardDebitLimitDto);

        //VERIFY
        assertThatThrownBy(createOrderMethodInvocation).isInstanceOf(EntityNotFoundException.class);
        verify(cardRepository, never()).save(card);
    }

    @Test
    @DisplayName("if card with incoming card id was found then return card number")
    void getCardNumberByCardId_shouldReturnAccountNumberDto(){
        //ARRANGE
        when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(cardNumber));
        when(cardMapper.toCardNumberDto(cardNumber.getCardNumber())).thenReturn(cardNumberDto);

        //ACT
        CardNumberDto result = cardService.getCardNumberByCardId(CARD_ID);

        //VERIFY
        assertThat(result).isNotNull();
        assertThat(result.toString()).isEqualTo(cardNumberDto.toString());
    }

    @Test
    @DisplayName("if card with incoming card number wasn't found then throws EntityNotFoundException")
    void getCardNumberByCardId_shouldThrow(){
        //ARRANGE
        when(cardRepository.findById(CARD_ID)).thenReturn(Optional.empty());

        //ACT
        ThrowingCallable getCardNumberByCardIdMethodInvocation = ()-> cardService.getCardNumberByCardId(CARD_ID);

        //VERIFY
        assertThatThrownBy(getCardNumberByCardIdMethodInvocation).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("If card with incoming card number wasn't found")
    void changePinCodeDebitCard_ifDebitCardLimitNotFound_thenThrow() {
        //ARRANGE
        when(cardRepository.findByAccountClientIdAndCardNumber(CLIENT_ID, CARD_NUMBER)).thenReturn(Optional.empty());

        //ACT
        ThrowingCallable createChangePinCodeDebitCardInvocation = () -> cardService.changePinCodeDebitCard(CLIENT_ID, newPinCodeDebitCardDto);

        //VERIFY
        assertThatThrownBy(createChangePinCodeDebitCardInvocation).isInstanceOf(EntityNotFoundException.class);

    }

    private void verifyDebitCardLimit(Card card) {
        assertSoftly(softAssertions -> {
            BigDecimal limit = card.getTransactionLimit();
            BigDecimal newLimit = cardDebitLimitDto.getTransactionLimit();
            softAssertions.assertThat(limit)
                    .withFailMessage("Transaction limit should be " + newLimit
                            + " instead of " + limit)
                    .isEqualTo(newLimit);
        });
    }

    @Test
    @DisplayName("If debit card successfully delete then return No Content")
    void deleteDebitCard_shouldReturnNoContent() {
        //ARRANGE
        when(cardRepository.findByAccountClientIdAndId(CLIENT_ID, CARD_ID)).thenReturn(Optional.of(card));

        //ACT
        cardService.deleteDebitCard(CLIENT_ID, CARD_ID);

        //VERIFY
        verify(cardRepository).deleteById(CARD_ID);
    }

    @Test
    @DisplayName("If debit card for deleting was not found then throw exception")
    void deleteCreditCard_ifCardNotFound_thenThrow(){
        //ARRANGE
        when(cardRepository.findByAccountClientIdAndId(CLIENT_ID, CARD_ID)).thenReturn(Optional.empty());

        //ACT
        ThrowingCallable changeLimitMethod = () -> cardService.deleteDebitCard(CLIENT_ID, CARD_ID);

        //VERIFY
        assertThatThrownBy(changeLimitMethod).isInstanceOf(EntityNotFoundException.class);
        verify(cardRepository, never()).deleteById(CARD_ID);
    }

    @Test
    @DisplayName("If debit card successfully write off sum then return true")
    void writeOffSum_shouldReturnTrue() {
        //ARRANGE
        when(cardRepository.findByCardNumber(any(String.class)))
                .thenReturn(Optional.of(card2));

        BigDecimal sum = card2.getBalance().subtract(createPaymentDepositDto.getSum());
        card2.setBalance(sum);
        when(cardRepository.saveAndFlush(any(Card.class))).thenReturn(card2);

        //ACT
        Boolean response = cardService.writeOffSum(CLIENT_ID, createPaymentDepositDto);

        //VERIFY
        verify(cardRepository).saveAndFlush(card2);
        assertThat(response).isEqualTo(true);
    }

    @Test
    @DisplayName("If debit card was not found then throw EntityNotFoundException")
    void writeOffSum_ifNotFoundEntity_thenThrowEntityNotFoundException() {
        //ARRANGE
        when(cardRepository.findByCardNumber(any(String.class)))
                .thenThrow(new EntityNotFoundException("Card with card number " +
                        createPaymentDepositDto.getRemitterCardNumber() + " wasn't found"));

        //ACT
        ThrowingCallable response = () -> cardService.writeOffSum(CLIENT_ID, createPaymentDepositDto);

        //VERIFY
        Assertions.assertThatThrownBy(response).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("If debit card has not enough balance then throw RuntimeException")
    void writeOffSum_ifBalanceIsNotEnough_thenThrowRuntimeException() {
        //ARRANGE
        createPaymentDepositDto.setSum(BigDecimal.valueOf(15000.0));
        when(cardRepository.findByCardNumber(any(String.class)))
                .thenReturn(Optional.of(card2));

        //ACT
        ThrowingCallable response = () -> cardService.writeOffSum(CLIENT_ID, createPaymentDepositDto);

        //VERIFY
        Assertions.assertThatThrownBy(response).isInstanceOf(RuntimeException.class);
    }

}