package by.afinny.deposit.unit.kafka;

import by.afinny.deposit.dto.kafka.CardEvent;
import by.afinny.deposit.entity.constant.CardStatus;
import by.afinny.deposit.integration.CardStatusTopicListener;
import by.afinny.deposit.service.CardService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class CardStatusTopicListenerTest {

    @Mock
    private CardService cardService;

    @InjectMocks
    private CardStatusTopicListener cardStatusTopicListener;

    private final UUID CARD_ID = UUID.fromString("f0fca2da-e7d2-11ec-8fea-0242ac120002");
    private final CardStatus NEW_CARD_STATUS = CardStatus.BLOCKED;

    private CardEvent event;

    @BeforeAll
    void setUp() {
        event = CardEvent.builder()
                .cardId(CARD_ID)
                .cardStatus(NEW_CARD_STATUS)
                .build();
    }

    @Test
    @DisplayName("When get request verify passed values through the service equality")
    void onRequestUpdateCardStatusEvent() {
        //ARRANGE
        ArgumentCaptor<UUID> cardIdCaptor = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<CardStatus> cardStatusCaptor = ArgumentCaptor.forClass(CardStatus.class);
        //ACT
        cardStatusTopicListener.onRequestUpdateCardStatusEvent(new GenericMessage<>(event));
        //VERIFY
        verify(cardService).modifyCardStatus(cardIdCaptor.capture(), cardStatusCaptor.capture());
        verifyEvent(cardIdCaptor.getValue(), cardStatusCaptor.getValue());
    }

    private void verifyEvent(UUID cardIdCaptor, CardStatus cardStatusCaptor) {
        assertSoftly((softAssertions) -> {
            softAssertions.assertThat(cardStatusCaptor).isEqualTo(event.getCardStatus());
            softAssertions.assertThat(cardIdCaptor).isEqualTo(event.getCardId());
        });
    }
}