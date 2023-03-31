package by.afinny.deposit.unit.mapper;

import by.afinny.deposit.dto.AccountNumberDto;
import by.afinny.deposit.dto.CardDto;
import by.afinny.deposit.dto.CardNumberDto;
import by.afinny.deposit.dto.RequestNewCardDto;
import by.afinny.deposit.dto.kafka.CardEvent;
import by.afinny.deposit.dto.kafka.ConsumerNewCardEvent;
import by.afinny.deposit.dto.kafka.ProducerNewCardEvent;
import by.afinny.deposit.entity.Card;
import by.afinny.deposit.entity.CardProduct;
import by.afinny.deposit.entity.constant.CardStatus;
import by.afinny.deposit.entity.constant.DigitalWallet;
import by.afinny.deposit.entity.constant.PaymentSystem;
import by.afinny.deposit.mapper.CardMapper;
import by.afinny.deposit.mapper.CardMapperImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@ExtendWith({MockitoExtension.class})
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class CardMapperTest {

    private final CardMapper cardMapper = new CardMapperImpl();
    private final UUID CLIENT_ID = UUID.randomUUID();
    private final UUID CARD_ID = UUID.fromString("f0fca2da-e7d2-11ec-8fea-0242ac120002");
    private final CardStatus NEW_CARD_STATUS = CardStatus.BLOCKED;
    private List<Card> cards;
    private Card card;
    private Card cardNumber;
    private ConsumerNewCardEvent consumerNewCardEvent;
    private RequestNewCardDto requestNewCardDto;
    private CardNumberDto cardNumberDto;

    @BeforeEach
    void setUp() {
        cards = new ArrayList<>();
        card = Card.builder()
                .id(CARD_ID)
                .cardNumber("1337")
                .digitalWallet(DigitalWallet.APPLEPAY)
                .expirationDate(LocalDate.now().plusYears(3))
                .holderName("holderName")
                .status(CardStatus.ACTIVE)
                .transactionLimit(BigDecimal.ONE)
                .cardProduct(CardProduct.builder()
                        .id(1)
                        .cardName("cardName")
                        .paymentSystem(PaymentSystem.VISA)
                        .build())
                .build();
        cards.add(card);
        consumerNewCardEvent = ConsumerNewCardEvent.builder()
                .accountNumber("134")
                .holderName("holder Name")
                .digitalWallet(DigitalWallet.MIRPAY)
                .transactionLimit(BigDecimal.ONE)
                .status(CardStatus.ACTIVE)
                .expirationDate(LocalDate.now().plusYears(5))
                .cardProductId(1)
                .cardNumber("cardNumber").build();

        requestNewCardDto = RequestNewCardDto.builder()
                .productId(23).build();

        cardNumber = Card.builder()
                .cardNumber("4544554")
                .build();

        cardNumberDto = CardNumberDto.builder()
                .cardNumber("4544554")
                .build();

    }

    @Test
    @DisplayName("Verify List<CardDto> fields settings")
    void toCardsDto_shouldReturnListCardDto() {
        List<CardDto> result = cardMapper.toCardsDto(cards);

        verifyCardList(cards, result);
    }

    @Test
    @DisplayName("Verify card event dto fields setting")
    void toToCardEvent() {
        //ACT
        CardEvent cardEvent = cardMapper.toCardEvent(CARD_ID, NEW_CARD_STATUS);
        //VERIFY
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(cardEvent.getCardId()).isEqualTo(CARD_ID);
            softAssertions.assertThat(cardEvent.getCardStatus()).isEqualTo(NEW_CARD_STATUS);
        });
    }

    @Test
    @DisplayName("Verify Card fields settings")
    void toCard_shouldReturnCard() {
        Card result = cardMapper.toCard(consumerNewCardEvent);

        verifyCard(result, consumerNewCardEvent);
    }

    @Test
    @DisplayName("Verify ProducerNewCardEvent fields settings")
    void toProducerNewCardEvent_shouldReturnProducerNewCardEvent() {
        ProducerNewCardEvent result = cardMapper.toProducerNewCardEvent(CLIENT_ID, requestNewCardDto);

        verifyCard(result, requestNewCardDto);
    }

    @Test
    @DisplayName("Check correct mapping data")
    void mapCardNumberDto_thenReturn(){
        cardMapper.toCardNumberDto(cardNumber.getCardNumber());
        assertSoftly(softAssertions ->
                softAssertions.assertThat(cardNumber.getCardNumber())
                        .withFailMessage("CardNumber should be equals")
                        .isEqualTo(cardNumberDto.getCardNumber())
        );
    }

    private void verifyCardList(List<Card> cards, List<CardDto> cardDtos) {
        cards.sort(Comparator.comparing(Card::getCardNumber));
        cardDtos.sort(Comparator.comparing(CardDto::getCardNumber));

        assertThat(cards).hasSameSizeAs(cardDtos);

        for (int i = 0; i < cards.size(); i++) {
            verifyCard(cards.get(i), cardDtos.get(i));
        }
    }

    private void verifyCard(Card card, CardDto cardDto) {
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(card.getId())
                    .withFailMessage("CardId should be equals")
                    .isEqualTo(cardDto.getCardId());
            softAssertions.assertThat(card.getCardNumber())
                    .withFailMessage("CardNumber should be equals")
                    .isEqualTo(cardDto.getCardNumber());
            softAssertions.assertThat(card.getExpirationDate())
                    .withFailMessage("ExpirationDate should be equals")
                    .isEqualTo(cardDto.getExpirationDate());
            softAssertions.assertThat(card.getHolderName())
                    .withFailMessage("HolderName should be equals")
                    .isEqualTo(cardDto.getHolderName());
            softAssertions.assertThat(card.getTransactionLimit())
                    .withFailMessage("TransactionLimit should be equals")
                    .isEqualTo(cardDto.getTransactionLimit());
            softAssertions.assertThat(card.getDigitalWallet())
                    .withFailMessage("DigitalWallet should be equals")
                    .isEqualTo(cardDto.getDigitalWallet());
            softAssertions.assertThat(card.getStatus())
                    .withFailMessage("Status should be equals")
                    .isEqualTo(cardDto.getStatus());
            softAssertions.assertThat(card.getIsDefault())
                    .withFailMessage("IsDefault should be equals")
                    .isEqualTo(cardDto.getIsDefault());
            softAssertions.assertThat(card.getCardProduct().getId())
                    .withFailMessage("CardProduct should be equals")
                    .isEqualTo(cardDto.getCardProductId());
            softAssertions.assertThat(card.getCardProduct().getCardName())
                    .withFailMessage("CardName should be equals")
                    .isEqualTo(cardDto.getCardName());
            softAssertions.assertThat(card.getCardProduct().getPaymentSystem())
                    .withFailMessage("PaymentSystem should be equals")
                    .isEqualTo(cardDto.getPaymentSystem());
        });
    }

    private void verifyCard(Card card, ConsumerNewCardEvent cardEvent) {
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(card.getCardNumber())
                    .withFailMessage("CardNumber should be equals")
                    .isEqualTo(cardEvent.getCardNumber());
            softAssertions.assertThat(card.getExpirationDate())
                    .withFailMessage("ExpirationDate should be equals")
                    .isEqualTo(cardEvent.getExpirationDate());
            softAssertions.assertThat(card.getHolderName())
                    .withFailMessage("HolderName should be equals")
                    .isEqualTo(cardEvent.getHolderName());
            softAssertions.assertThat(card.getTransactionLimit())
                    .withFailMessage("TransactionLimit should be equals")
                    .isEqualTo(cardEvent.getTransactionLimit());
            softAssertions.assertThat(card.getDigitalWallet())
                    .withFailMessage("DigitalWallet should be equals")
                    .isEqualTo(cardEvent.getDigitalWallet());
            softAssertions.assertThat(card.getCardProduct().getId())
                    .withFailMessage("CardProductId should be equals")
                    .isEqualTo(cardEvent.getCardProductId());
            softAssertions.assertThat(card.getStatus())
                    .withFailMessage("Status should be equals")
                    .isEqualTo(cardEvent.getStatus());
        });
    }

    private void verifyCard(ProducerNewCardEvent newCardEvent, RequestNewCardDto requestNewCardDto) {
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(newCardEvent.getClientId())
                    .withFailMessage("ClientId should be equals")
                    .isEqualTo(CLIENT_ID);
            softAssertions.assertThat(newCardEvent.getRequestNewCardDto())
                    .withFailMessage("RequestNewCardDto should be equals")
                    .isEqualTo(requestNewCardDto);
        });
    }
}